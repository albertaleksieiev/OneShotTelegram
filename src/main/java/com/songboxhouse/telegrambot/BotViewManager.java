package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.anotations.InstanceProvider;
import com.songboxhouse.telegrambot.context.BotContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BotViewManager {
    private Object dependecyProvider;

    BotViewManager(Object dependecyProvider) {
        this.dependecyProvider = dependecyProvider;
    }

    <BV extends BotView> BotView getView(BotContext botContext, Class<BV> view) {
        try {
            Constructor<?> constructor = view.getConstructors()[0];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            List<Object> constructorObjects = new ArrayList<>();
            for (Class param : parameterTypes) {
                Object type;
                if (param.isAssignableFrom(botContext.getClass())) {
                    type = botContext;
                } else {
                    type = buildTypeFromDependencyProvider(param);
                }
                constructorObjects.add(type);
            }
            Object[] objects = constructorObjects.toArray(new Object[constructorObjects.size()]);

            return (BotView) constructor.newInstance(objects);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        System.out.println("Cannot build view!");
        return null;
    }

    private Object buildTypeFromDependencyProvider(Class clazz) throws InvocationTargetException, IllegalAccessException {
        if (dependecyProvider == null) {
            System.out.println("Dependency provide is null, skip creating an object");
            return null;
        }

        if (clazz.isAnnotationPresent(InstanceProvider.class)) {
            System.out.println("Your object probably not anotated with @InstanceProvider`");
            return null;
        }

        Class dependecyProviderClass = dependecyProvider.getClass();

        Method[] methods = dependecyProviderClass.getMethods();
        for (Method method: methods) {
            if (method.getReturnType().equals(clazz)) {
                return method.invoke(dependecyProvider);
            }
        }

        System.out.println("no dependency creator function is founded for class " + clazz.getName());
        return null;
    }

}
