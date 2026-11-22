package com.meridian.payments.intake;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec; import java.math.BigDecimal; import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.time.*; import java.util.*; import java.util.regex.Pattern;
/** Provider raw-byte signature verification and mapping. Production secrets come from Key Vault. */
public final class WebhookIntakeService {
 private static final Pattern MX=Pattern.compile("POL-MX-[0-9]{9}"), CO=Pattern.compile("POL-CO-[0-9]{9}");
 private final ObjectMapper json=new ObjectMapper(); private final Inbox inbox; private final EventPublisher publisher; private final Clock clock;
 public WebhookIntakeService(Inbox i,EventPublisher p,Clock c){inbox=i;publisher=p;clock=c;}
 public Result quetzal(byte[] raw,String header,String secret,UUID correlation){
  if(header==null||secret==null)return Result.reject(401,"missing_signature");
  try{
   String[] h=header.split(","); String t=null; List<String> sigs=new ArrayList<>();
   for(String part:h){String[] kv=part.trim().split("=",2); if(kv.length!=2)throw new Bad("bad_signature_header"); if(kv[0].equals("t"))t=kv[1]; if(kv[0].equals("v1"))sigs.add(kv[1]);}
   if(t==null||sigs.isEmpty())throw new Bad("missing_signature");
   if(Math.abs(clock.instant().getEpochSecond()-Long.parseLong(t))>300)return Result.reject(401,"timestamp_outside_tolerance");
   String expected=HexFormat.of().formatHex(hmac(secret,(t+".").getBytes(UTF_8),raw));
   if(sigs.stream().noneMatch(s->safeHexEquals(expected,s)))return Result.reject(401,"invalid_signature");
   JsonNode r=json.readTree(raw), c=r.path("data").path("charge"); String id=req(r,"id"), tx=req(c,"id");
   String policy=policy(req(c,"reference"),MX); String type=req(r,"type");
   String status=switch(type){case "charge.succeeded"->"Captured";case "charge.failed"->"Failed";case "charge.refunded"->"Refunded";default->throw new Bad("unsupported_event_type");};
   long minor=c.path(type.equals("charge.refunded")?"amount_refunded":"amount").asLong(-1); if(minor<0)throw new Bad("invalid_amount");
   String currency=req(c,"currency").toUpperCase(Locale.ROOT); if(!currency.equals("MXN"))throw new Bad("invalid_currency");
   return accept("Quetzal:"+id,raw,"Quetzal","MX",tx,policy,status,BigDecimal.valueOf(minor,2),currency,Instant.ofEpochSecond(r.path("created").asLong()),correlation,id,opt(c,"failure_code"));
  }catch(Bad e){return Result.reject(400,e.getMessage());}catch(Exception e){return Result.reject(400,"malformed_request");}
 }
 public Result andina(byte[] raw,String timestamp,String signature,String secret,UUID correlation){
  if(timestamp==null||signature==null||secret==null)return Result.reject(401,"missing_signature");
  try{
   Instant signed=OffsetDateTime.parse(timestamp).toInstant();
   if(Duration.between(signed,clock.instant()).abs().compareTo(Duration.ofMinutes(10))>0)return Result.reject(401,"timestamp_outside_tolerance");
   byte[] supplied=Base64.getDecoder().decode(signature), expected=hmac(secret,(timestamp+".").getBytes(UTF_8),raw);
   if(!MessageDigest.isEqual(expected,supplied))return Result.reject(401,"invalid_signature");
   JsonNode r=json.readTree(raw), t=r.path("transaccion"); String tx=req(t,"id"), state=req(t,"estado");
   String policy=policy(req(t,"referencia"),CO);
   String status=switch(state){case "PENDIENTE"->"Pending";case "APROBADA"->"Captured";case "RECHAZADA"->"Failed";case "REVERSADA"->"Refunded";default->throw new Bad("unsupported_state");};
   BigDecimal amount=new BigDecimal(req(t,"valor")); if(amount.signum()<0||amount.scale()>2)throw new Bad("invalid_amount");
   String currency=req(t,"moneda"); if(!currency.equals("COP"))throw new Bad("invalid_currency");
   Instant occurred=OffsetDateTime.parse(req(t,"fecha_transaccion")).toInstant(); String id=req(r,"notificacion_id");
   return accept("Andina:"+tx+":"+state+":"+occurred,raw,"Andina","CO",tx,policy,status,amount,currency,occurred,correlation,id,opt(t,"motivo_reverso"));
  }catch(Bad e){return Result.reject(400,e.getMessage());}catch(Exception e){return Result.reject(400,"malformed_request");}
 }
 private Result accept(String key,byte[] raw,String provider,String country,String tx,String policy,String status,BigDecimal amount,String currency,Instant occurred,UUID cid,String sourceId,String reason){
  Instant received=clock.instant(); if(!inbox.accept(key,raw.clone(),provider,cid.toString(),received))return new Result(provider.equals("Andina")?200:202,true,"duplicate");
  String eventId=UUID.nameUUIDFromBytes(key.getBytes(UTF_8)).toString();
  publisher.publish(new CanonicalPaymentEvent(1,eventId,provider,country,provider+":"+tx,tx,policy,status,amount,currency,occurred,received,cid,sourceId,reason));
  return new Result(provider.equals("Andina")?200:202,true,"accepted");
 }
 private static final java.nio.charset.Charset UTF_8=StandardCharsets.UTF_8;
 private static String policy(String s,Pattern p){String v=s.trim().toUpperCase(Locale.ROOT);if(!p.matcher(v).matches())throw new Bad("invalid_policy_reference");return v;}
 private static String req(JsonNode n,String k){JsonNode v=n.get(k);if(v==null||v.isNull()||v.asText().isBlank())throw new Bad("missing_"+k);return v.asText();}
 private static String opt(JsonNode n,String k){JsonNode v=n.get(k);return v==null||v.isNull()?null:v.asText();}
 private static byte[] hmac(String s,byte[] prefix,byte[] raw)throws Exception{Mac m=Mac.getInstance("HmacSHA256");m.init(new SecretKeySpec(s.getBytes(UTF_8),"HmacSHA256"));m.update(prefix);return m.doFinal(raw);}
 private static boolean safeHexEquals(String a,String b){try{return MessageDigest.isEqual(HexFormat.of().parseHex(a),HexFormat.of().parseHex(b));}catch(IllegalArgumentException e){return false;}}
 private static class Bad extends RuntimeException{Bad(String m){super(m);}}
 public record Result(int httpStatus,boolean accepted,String code){static Result reject(int s,String c){return new Result(s,false,c);}}
}