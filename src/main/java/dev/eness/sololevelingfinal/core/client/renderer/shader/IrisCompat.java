package dev.eness.sololevelingfinal.core.client.renderer.shader;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import org.slf4j.Logger;

public final class IrisCompat {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static volatile boolean initialized;
   private static boolean available;
   private static Object irisApi;
   private static Method isShaderPackInUseMethod;
   private static Method isRenderingShadowPassMethod;

   private IrisCompat() {
   }

   public static boolean isShaderPackInUse() {
      initialize();
      return invokeBoolean(isShaderPackInUseMethod);
   }

   public static boolean isRenderingShadowPass() {
      initialize();
      return invokeBoolean(isRenderingShadowPassMethod);
   }

   public static boolean isAvailable() {
      initialize();
      return available;
   }

   private static boolean invokeBoolean(Method method) {
      if (available && method != null) {
         try {
            return Boolean.TRUE.equals(method.invoke(irisApi));
         } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static synchronized void initialize() {
      if (!initialized) {
         initialized = true;

         for (String className : new String[]{"net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi"}) {
            try {
               Class<?> apiClass = Class.forName(className);
               irisApi = apiClass.getMethod("getInstance").invoke(null);
               isShaderPackInUseMethod = apiClass.getMethod("isShaderPackInUse");
               isRenderingShadowPassMethod = apiClass.getMethod("isRenderingShadowPass");
               available = true;
               LOGGER.info("[SoloLeveling] Iris/Oculus detected ({}); world quad shaders will use deferred compatibility when a pack is active.", className);
               return;
            } catch (ReflectiveOperationException | LinkageError ignored) {
               irisApi = null;
               isShaderPackInUseMethod = null;
               isRenderingShadowPassMethod = null;
            }
         }

         available = false;
         LOGGER.info("[SoloLeveling] Iris/Oculus not detected; world quad shaders will use the normal render path.");
      }
   }
}
