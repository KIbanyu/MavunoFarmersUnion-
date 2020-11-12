package com.mavuno.famers.union.mavuno.configs;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Date;


public class Utils {


    private static String secret = "kibzdevtasks";

    public static boolean isTokenExpired(String token) {
        boolean tokenExpired = true;

        if (token != null) {
            long secondsInMilli = 1000 * 60;

            if (getIssuedAtDateFromToken(token) != null) {
                long expiryTimeSeconds = getExpirationDateFromToken(token).getTime() / secondsInMilli;


                Date date = new Date(System.currentTimeMillis());

                long currentTimeSeconds = date.getTime() / secondsInMilli;

                if (currentTimeSeconds < expiryTimeSeconds)
                    tokenExpired = false;
            }
        }

        return tokenExpired;
    }

    public static Date getIssuedAtDateFromToken(String token) {

        if (token != null) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }

        Date issueAt;
        try {
            final Claims claims = getAllClaimsFromToken(token);
            issueAt = claims.getIssuedAt();
        } catch (Exception e) {
            issueAt = null;
        }
        return issueAt;
    }

    public static Claims getAllClaimsFromToken(String token) {
        Claims claims;

        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    public static Date getExpirationDateFromToken(String token) {

        if (token != null) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }

        Date expiration;
        try {
            final Claims claims = getAllClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }



}
