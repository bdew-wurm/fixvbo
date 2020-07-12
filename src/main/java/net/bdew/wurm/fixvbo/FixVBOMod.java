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

    private void patchMaterial(ClassPool classPool, CtClass cls) throws NotFoundException, CannotCompileException {
        CtClass ctMaterialInstance = classPool.get("com.wurmonline.client.renderer.MaterialInstance");

        cls.addField(new CtField(ctMaterialInstance, "material", cls));

        for (CtConstructor cons : cls.getDeclaredConstructors()) {
            cons.insertAfter(
                    "           if (com.wurmonline.client.util.GLHelper.useDeferredShading()) {" +
                            "            this.material = com.wurmonline.client.renderer.Material.load(\"material.simple\").instance();" +
                            "        } else {" +
                            "            this.material = null;" +
                            "        }");
            logInfo(String.format("Patched constructor %s", cons.getLongName()));
        }

        for (CtMethod render : cls.getDeclaredMethods("render")) {
            render.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("queue")) {
                        logInfo(String.format("Patched render method %s", m.where().getLongName()));
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
        }
    }

    @Override
    public void preInit() {
        ClassPool classPool = HookManager.getInstance().getClassPool();

        try {
            patchMaterial(classPool, classPool.get("com.wurmonline.client.renderer.effects.LightBeamEffect"));
            patchMaterial(classPool, classPool.get("com.wurmonline.client.renderer.cell.TilesOverlay"));

            classPool.get("com.wurmonline.client.renderer.cell.CellRenderable")
                    .getMethod("addEffect", "(BBBBB)V")
                    .insertBefore("if ($1==2 && $2>0) $2=(byte)-128;");

        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }
}
