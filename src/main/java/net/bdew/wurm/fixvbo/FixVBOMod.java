package net.bdew.wurm.fixvbo;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FixVBOMod implements WurmClientMod, PreInitable {
    private static final Logger logger = Logger.getLogger("FixVBO");

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    @Override
    public void preInit() {
        ClassPool classPool = HookManager.getInstance().getClassPool();


        try {
            CtClass ctMaterialInstance = classPool.get("com.wurmonline.client.renderer.MaterialInstance");
            CtClass ctLightBeam = classPool.get("com.wurmonline.client.renderer.effects.LightBeamEffect");

            ctLightBeam.addField(new CtField(ctMaterialInstance, "material", ctLightBeam));

            for (CtConstructor cons : ctLightBeam.getDeclaredConstructors()) {
                cons.insertAfter(
                        "           if (com.wurmonline.client.util.GLHelper.useDeferredShading()) {" +
                                "            this.material = com.wurmonline.client.renderer.Material.load(\"material.simple\").instance();" +
                                "        } else {" +
                                "            this.material = null;" +
                                "        }");
                logInfo(String.format("Patched constructor %s", cons.getLongName()));
            }

            ctLightBeam.getMethod("render", "(Lcom/wurmonline/client/renderer/backend/Queue;F)V")
                    .instrument(new ExprEditor() {
                        @Override
                        public void edit(MethodCall m) throws CannotCompileException {
                            if (m.getMethodName().equals("queue")) {
                                m.replace(
                                        "   if (this.material != null) {" +
                                                "       $1.materialInstance = this.material;" +
                                                "       $1.program = this.material.getProgram();" +
                                                "       $1.bindings = this.material.getProgramBindings();" +
                                                "     }" +
                                                "     $proceed($$);");
                            }
                        }
                    });


        } catch (NotFoundException | CannotCompileException e) {
            e.printStackTrace();
        }
    }
}
