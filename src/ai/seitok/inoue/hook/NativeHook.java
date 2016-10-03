package ai.seitok.inoue.hook;

public class NativeHook {

    static {
        System.load("C:\\Users\\Tsunko\\.CLion2016.2\\system\\cmake\\generated\\InoueNativeHook-9df7427a\\9df7427a\\Debug\\libInoueNativeHook.dll");
    }

    private int keychar;
    private Runnable callback;

    public NativeHook(int keychar, Runnable callback){
        this.keychar = keychar;
        this.callback = callback;
    }

    public native void hookKey();
    public native void unhookKey();

    public void invokeCallback(){
        callback.run();
    }

}
