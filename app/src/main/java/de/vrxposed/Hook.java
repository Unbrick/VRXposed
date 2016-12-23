package de.vrxposed;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.contains("fiducia"))
            return;

        /*
        * Block the killing of the VRBanking app
        * */
        findAndHookMethod("no.promon.shield.Report", lpparam.classLoader, "report", String.class, new XCMethodReplacement());

        /*
        * Override the check whether the app is in currently at developement #1
        * Only a temporary solution
        * */
        findAndHookMethod("java.lang.String", lpparam.classLoader, "equalsIgnoreCase", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject == "debugwithoutpromon" && param.args[0] == "release") {
                    setAdditionalInstanceField(param.thisObject, "pronom", true);
                } else {
                    setAdditionalInstanceField(param.thisObject, "pronom", false);
                }
            }
        });

        /*
        * Override the check whether the app is in currently at developement #2
        * Only a temporary solution
        * */
        findAndHookMethod("java.lang.String", lpparam.classLoader, "equalsIgnoreCase", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                boolean pronom = (boolean) getAdditionalInstanceField(param.thisObject, "pronom");
                try {
                    if (pronom) {
                        param.setResult(true);
                    }
                } catch (Exception ignored) {
                }
            }
        });

        /*
        * Hook classes2.dex
        * disable starting of Pronom
        * */
        findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                findAndHookMethod("no.promon.shield.LibshieldStarter", lpparam.classLoader, "startLibshieldWithContext", Context.class, new XCMethodReplacement());
            }
        });

        /*
        * Hook normal classes.dex
        * disable starting of Pronom
        * */
        findAndHookMethod("no.promon.shield.LibshieldStarter", lpparam.classLoader, "startLibshieldWithContext", Context.class, new XCMethodReplacement());

        /*
        * Just in case, kill deviceManagement (XposedCheck)
        * */
        findAndHookMethod("de.fiducia.smartphone.android.banking.context.e", lpparam.classLoader, "initializeDeviceManagement", Context.class, new XCMethodReplacement());
    }

    private class XCMethodReplacement extends XC_MethodReplacement{

        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            return null;
        }
    }
}
