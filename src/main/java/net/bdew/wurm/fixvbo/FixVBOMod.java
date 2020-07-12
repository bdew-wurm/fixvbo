package net.bdew.wurm.fixvbo;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
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
        ResPatcher resPatcher = new ResPatcher();

        try {
            CtClass ctResources = classPool.get("com.wurmonline.client.resources.Resources");
            ctResources.getMethod("getResourceAsString", "(Ljava/lang/String;)Ljava/lang/String;")
                    .insertAfter("return net.bdew.wurm.fixvbo.ResPatcher.filterResource($1, $_);");
        } catch (NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }
}
