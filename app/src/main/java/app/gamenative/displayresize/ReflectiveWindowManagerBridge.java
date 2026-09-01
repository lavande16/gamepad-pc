package app.gamenative.displayresize;

import android.graphics.Point;
import android.os.IBinder;
import android.os.Parcel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class ReflectiveWindowManagerBridge implements WindowManagerCommandExecutor.Bridge {
    private static final int DEFAULT_DISPLAY = 0;
    private static final String WINDOW_MANAGER_DESCRIPTOR = "android.view.IWindowManager";
    private static final int TRANSACTION_SET_FORCED_DISPLAY_SIZE =
            IBinder.FIRST_CALL_TRANSACTION + 6;
    private static final int TRANSACTION_CLEAR_FORCED_DISPLAY_SIZE =
            IBinder.FIRST_CALL_TRANSACTION + 7;

    private IBinder windowBinder;
    private Object windowManager;

    @Override
    public DisplayDimensions initialSize() throws Exception {
        Point size = new Point();
        invoke("getInitialDisplaySize", new Class<?>[]{int.class, Point.class},
                DEFAULT_DISPLAY, size);
        return new DisplayDimensions(size.x, size.y);
    }

    @Override
    public DisplayDimensions baseSize() throws Exception {
        Point size = new Point();
        invoke("getBaseDisplaySize", new Class<?>[]{int.class, Point.class},
                DEFAULT_DISPLAY, size);
        return new DisplayDimensions(size.x, size.y);
    }

    @Override
    public void setForcedSize(int width, int height) throws Exception {
        transactDisplaySize(TRANSACTION_SET_FORCED_DISPLAY_SIZE, width, height);
    }

    @Override
    public void clearForcedSize() throws Exception {
        transactDisplaySize(TRANSACTION_CLEAR_FORCED_DISPLAY_SIZE);
    }

    private Object invoke(String name, Class<?>[] parameterTypes, Object... arguments)
            throws Exception {
        Object service = getWindowManager();
        Method method = service.getClass().getMethod(name, parameterTypes);
        try {
            return method.invoke(service, arguments);
        } catch (InvocationTargetException invocationError) {
            Throwable cause = invocationError.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw invocationError;
        }
    }

    private Object getWindowManager() throws Exception {
        if (windowManager != null) {
            return windowManager;
        }
        Class<?> serviceManager = Class.forName("android.os.ServiceManager");
        Method getService = serviceManager.getDeclaredMethod("getService", String.class);
        windowBinder = (IBinder) getService.invoke(null, "window");
        if (windowBinder == null) {
            throw new IllegalStateException("无法取得 WindowManager 服务");
        }
        Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
        Method asInterface = stub.getDeclaredMethod("asInterface", IBinder.class);
        windowManager = asInterface.invoke(null, windowBinder);
        if (windowManager == null) {
            throw new IllegalStateException("无法连接 WindowManager 服务");
        }
        return windowManager;
    }

    private IBinder getWindowBinder() throws Exception {
        if (windowBinder == null) {
            getWindowManager();
        }
        return windowBinder;
    }

    private void transactDisplaySize(int transactionCode, int... size) throws Exception {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(WINDOW_MANAGER_DESCRIPTOR);
            data.writeInt(DEFAULT_DISPLAY);
            for (int value : size) {
                data.writeInt(value);
            }
            if (!getWindowBinder().transact(transactionCode, data, reply, 0)) {
                throw new IllegalStateException("WindowManager 未处理分辨率请求");
            }
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
