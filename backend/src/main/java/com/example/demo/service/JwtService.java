package com.example.demo.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;

    // 1. 負責從 token 中提取使用者名稱 (Subject)

    public String getUsername(String token){
        // TODO: 實作提取邏輯，提示：使用 extractClaim 方法

        return extractClaim(token, Claims::getSubject);
    }

    // 2. 負責從 token 中提取特定的 Claim (泛型方法)
    // <T>意思是不知道回傳類型，使用一個代號先給它
    // 這個函式的作用是，裡面new了一個Claim物件(傳入token得到Claim)，這樣你不需要在每個會用到Claim的地方都寫一次new
    // Claim是從token解碼完後得到的物件，其屬性包含Username (Subject), Expiration (過期時間), Issued At (發證時間), etc...
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. 產生 Token (不帶額外 claims)
    public String generateToken(String username){
        return generateToken(new HashMap<>(), username);
    }

    // 4. 產生 Token (帶額外 claims，如 userId, role 等)
    public String generateToken(Map<String, Object> extraClaims, String username){
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))// 設定過期時間
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 5. 驗證 Token 是否有效
    public boolean isTokenValid(String token, String username){
        // TODO: 實作驗證邏輯
        // 邏輯：token 內的 username 必須等於傳入的 username，且 token 未過期
        return  (username.equals(getUsername(token)) && !isTokenExpired(token));
    }

    // 私有方法：判斷是否過期
    public boolean isTokenExpired(String token){
        return (extractExpiration(token).before(new Date()));
    }


    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
