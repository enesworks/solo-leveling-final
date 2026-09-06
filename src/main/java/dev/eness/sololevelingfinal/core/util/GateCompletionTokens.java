package dev.eness.sololevelingfinal.core.util;

public final class GateCompletionTokens {
   private GateCompletionTokens() {
   }

   public static boolean contains(String encodedTokens, String gateId) {
      String token = tokenFor(gateId);
      if (encodedTokens != null && !encodedTokens.isEmpty() && !token.isEmpty()) {
         int searchFrom = 0;

         while (searchFrom < encodedTokens.length()) {
            int match = encodedTokens.indexOf(token, searchFrom);
            if (match < 0) {
               return false;
            }

            if (match == 0 || encodedTokens.charAt(match - 1) == ',') {
               return true;
            }

            searchFrom = match + 1;
         }

         return false;
      } else {
         return false;
      }
   }

   public static String remove(String encodedTokens, String gateId) {
      if (encodedTokens != null && !encodedTokens.isEmpty()) {
         String token = tokenFor(gateId);
         if (token.isEmpty()) {
            return encodedTokens;
         }

         StringBuilder result = null;
         int copyFrom = 0;
         int searchFrom = 0;

         while (searchFrom < encodedTokens.length()) {
            int match = encodedTokens.indexOf(token, searchFrom);
            if (match < 0) {
               break;
            }

            if (match != 0 && encodedTokens.charAt(match - 1) != ',') {
               searchFrom = match + 1;
            } else {
               if (result == null) {
                  result = new StringBuilder(encodedTokens.length());
               }

               result.append(encodedTokens, copyFrom, match);
               copyFrom = match + token.length();
               searchFrom = copyFrom;
            }
         }

         return result == null ? encodedTokens : result.append(encodedTokens, copyFrom, encodedTokens.length()).toString();
      } else {
         return encodedTokens == null ? "" : encodedTokens;
      }
   }

   private static String tokenFor(String gateId) {
      return gateId != null && !gateId.isEmpty() && gateId.indexOf(44) < 0 ? gateId + "," : "";
   }
}
