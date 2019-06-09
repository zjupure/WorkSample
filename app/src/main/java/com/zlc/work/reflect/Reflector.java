package com.zlc.work.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * author: liuchun
 * date: 2019-06-09
 */
public class Reflector {

    private final Object mCaller;
    private final Class<?> mClass;

    public static Reflector on(String name) {
        try {
            Class<?> clazz = Class.forName(name);
            return new Reflector(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Reflector(name);
    }

    public static Reflector on(Class<?> clazz) {
        return new Reflector(clazz);
    }

    public static Reflector on(Object object) {
        return new Reflector(object);
    }


    private Reflector(Class<?> clazz) {
        mCaller = null;
        mClass = clazz;
    }

    private Reflector(Object object) {
        mCaller = object;
        mClass = object.getClass();
    }

    private Reflector(Object object, Class<?> clazz) {
        mCaller = object;
        mClass = clazz != null ? clazz : object.getClass();
    }

    public <T> T get(String name) {
        try {
            Field field = getField(name);
            return (T)field.get(mCaller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set(String name, Object value) {
        try {
            Field field = getField(name);
            field.set(mCaller, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public <T> T call(String name) {
        return call(name, new Class<?>[0]);
    }

    public  <T> T call(String name, Class<?>[] paramTypes, Object... args) {
        Class<?>[] types = types(args);
        Method method = null;
        try {
            if (paramTypes != null) {
                try {
                    method = getMethod(name, paramTypes);
                } catch (NoSuchMethodException e) {
                    method = getMethod(name, types);
                }
            } else {
                method = getMethod(name, types);
            }
        } catch (NoSuchMethodException e) {
            // 精确匹配失败, 找个最相似的方法
            try {
                method = getSimilarMethod(name, types);
            } catch (NoSuchMethodException ignore) {
                ignore.printStackTrace();
            }
        }

        if (method == null) {
            return null;
        }

        try {
            if (method.getReturnType() == void.class) {
                method.invoke(mCaller, args);
            } else {
                return (T) method.invoke(mCaller, args);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Field getField(String name) throws NoSuchFieldException {
        Class<?> type = mClass;
        Field field = null;
        try {
            // try get public field first
            field = type.getField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            // try get private field
            do {
                try {
                    field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignore) {
                    ignore.printStackTrace();
                }
                type = type.getSuperclass();
            } while (type != null);
            throw new NoSuchFieldException(e.getMessage());
        }
    }

    private Method getMethod(String name, Class<?>[] paramTypes) throws NoSuchMethodException{
        Class<?> type = mClass;
        Method method = null;
        try {
            // try get public method first
            method = type.getMethod(name, paramTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            // try get private method then
            do {
                try {
                    method = type.getDeclaredMethod(name, paramTypes);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ingore) {
                    ingore.printStackTrace();
                }
                type = type.getSuperclass();
            } while (type != null);
            throw new NoSuchMethodException(e.getMessage());
        }
    }

    private Method getSimilarMethod(String name, Class<?>[] paramTypes) throws NoSuchMethodException{
        Class<?> type = mClass;
        try {
            for (Method method : type.getMethods()) {
                if (isSimilarSignature(method, name, paramTypes)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        }

        do {
            try {
                for (Method method : type.getDeclaredMethods()) {
                    if (isSimilarSignature(method, name, paramTypes)) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
            type = type.getSuperclass();
        } while (type != null);
        throw new NoSuchMethodException("No similar method found in class" + mClass.getName());
    }

    private boolean isSimilarSignature(Method method, String name, Class<?>[] paramTypes) {
        if (name.equals(method.getName())) {
            Class<?>[] declaredTypes = method.getParameterTypes();
            if (declaredTypes.length == paramTypes.length) {
                for (int i = 0; i < declaredTypes.length; i++) {
                    if (paramTypes[i] == NULL.class) {
                        // 参数传了null，可以匹配任意类型
                        continue;
                    }

                    if (wrapper(declaredTypes[i]).isAssignableFrom(wrapper(paramTypes[i]))) {
                        // 方法声明的参数类型是实际传参的父类或父接口
                        continue;
                    }


                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private Class<?>[] types(Object... args) {
        if (args == null) {
            return new Class[0];
        }
        Class<?>[] result = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            result[i] = arg == null ? NULL.class : arg.getClass();
        }
        return result;
    }

    private static Class<?> wrapper(Class<?> type) {
        Class<?> wrapper = type;
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                wrapper = Boolean.class;
            } else if (type == int.class) {
                wrapper = Integer.class;
            } else if (type == long.class) {
                wrapper = Long.class;
            } else if (type == short.class) {
                wrapper = Short.class;
            } else if (type == byte.class) {
                wrapper = Byte.class;
            } else if (type == double.class) {
                wrapper = Double.class;
            } else if (type == float.class) {
                wrapper = Float.class;
            } else if (type == char.class) {
                wrapper = Character.class;
            } else if (type == void.class) {
                wrapper = Void.class;
            }
        }
        return wrapper;
    }

    private static class NULL {}
}
