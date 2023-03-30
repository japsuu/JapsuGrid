package com.github.fernthedev.fernutils;

import com.github.fernthedev.fernutils.thread.ThreadUtils;
import com.github.fernthedev.fernutils.thread.multiple.TaskInfoList;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.StopWatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
@Data
@ToString
/**
 * Testing
 */
public class Settings {

    @SettingValue
    private int port = 2000;

    @SettingValue
    private String password = "password";

    @SettingValue(name = "multicast")
    private boolean useMulticast = false;

    @SettingValue(name = "usepassword")
    private boolean passwordRequiredForLogin = false;

    @SettingValue(name = "usenativetransport")
    private boolean useNativeTransport = true;


    @Deprecated
    public void setNewValue(@NonNull String oldValue, @NonNull String newValue) {

        Object value = newValue;

        switch (newValue.toLowerCase()) {
            case "true":
                value = true;
                break;
            case "false":
                value = false;
                break;
        }

        if (StringUtils.isNumeric(newValue)) {
            try {
                value = Integer.parseInt(newValue);
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Incorrect integer value");
            }
        }

        if (newValue.equals("") || oldValue.equals("")) {
            throw new IllegalArgumentException("Values cannot be empty");
        }


        switch (oldValue.toLowerCase()) {
            case "password":
                setPassword((String) value);
                break;
            default:
                throw new IllegalArgumentException("No such value named " + oldValue + " found");
        }

    }

    /**
     * @deprecated Use {@link #multiThread(boolean, boolean)} instead
     */
    @Deprecated
    public List<String> getSettingNames(boolean editable) {
        List<String> stringList = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                if (!editable) continue;

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();


                stringList.add(name);
            }
        }

        return stringList;
    }

    public Map<String, List<String>> multiThread(boolean editable, boolean log) {
//        Map<String, List<String>> stringList = new HashMap<>();
//        System.out.println("Getting setting values");

        long time;

        Map<String, List<String>> returnValues = new HashMap<>();
        TaskInfoList ob;

        ob = ThreadUtils.runForLoopAsync(Arrays.asList(getClass().getDeclaredFields()), field -> {
            StopWatch stopwatchField = StopWatch.createStarted();

            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                if (!settingValue.editable() && editable) return null;

                String name = settingValue.name();

                List<String> possibleValues = new ArrayList<>(Arrays.asList(settingValue.values()));

                if (possibleValues.isEmpty()) {
                    // ENUM
                    if (field.isEnumConstant()) {
                        possibleValues = Arrays.stream(field.getClass().getEnumConstants()).map(s -> {
                            try {
                                return s.get(this).toString();
                            } catch (IllegalAccessException e) {
                                return null;
                            }
                        }).collect(Collectors.toList());
                    }
                    // Boolean
                    if (boolean.class.equals(field.getType())) {
                        possibleValues.add("true");
                        possibleValues.add("false");
                    }
                }


                if (name.equals("")) name = field.getName();

                if (log)
                    System.out.println("Field " + name + " took " + stopwatchField.getTime(TimeUnit.MILLISECONDS) + "ms");


                returnValues.put(name, possibleValues);
            }
            return null;
        });

        StopWatch stopwatch = StopWatch.createStarted();

        // 51 ms parallel

        ob.runThreads(ThreadUtils.ThreadExecutors.CACHED_THREADS.getExecutorService());

        ob.awaitFinish(2);

        stopwatch.stop();
        time = stopwatch.getTime(TimeUnit.MILLISECONDS);

        if (log)
            System.out.println("Took " + time + "ms for settings");

        return returnValues;
    }

    public Map<String, List<String>> singleThread(boolean editable, boolean log) {
        Map<String, List<String>> stringList = new HashMap<>();
        long time = 0;

        StopWatch stopwatch = StopWatch.createStarted();
        for (Field field : getClass().getDeclaredFields()) {

            if (log)
                System.out.println("Checking field " + field.getName() + " " + time);

            if (field.isAnnotationPresent(SettingValue.class)) {

                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                if (!editable) continue;

                String name = settingValue.name();



                List<String> possibleValues = new ArrayList<>(Arrays.asList(settingValue.values()));


                if (possibleValues.isEmpty()) {

                    // ENUM
                    if (field.isEnumConstant()) {

                        possibleValues = Arrays.stream(field.getClass().getEnumConstants()).map(s -> {
                            try {
                                return s.get(this).toString();
                            } catch (IllegalAccessException e) {
                                return null;
                            }
                        }).collect(Collectors.toList());


//                        Arrays.stream(constants).forEach(field1 -> {
//                            try {
//                                possibleValues.add(field1.get(this).toString());
//
//                            } catch (IllegalAccessException e) {
//                                e.printStackTrace();
//                            }
//                        });

                    }
                    // Boolean
                    if (boolean.class.equals(field.getType())) {
                        possibleValues.add("true");
                        possibleValues.add("false");
                    }
                }


                if (name.equals("")) name = field.getName();


                stringList.put(name, possibleValues);

                if (log)
                    System.out.println("Field " + name + " took " + stopwatch.getTime(TimeUnit.MILLISECONDS) + "ms");

            }
        }
        time = stopwatch.getTime(TimeUnit.MILLISECONDS);


        if (log)
            System.out.println("Took in total " + time + "ms");

        return stringList;
    }

    public void setValue(@NonNull String key, String val) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();

                if (name.equalsIgnoreCase(key)) {
                    try {

                        if (!settingValue.editable()) {
                            throw new IllegalArgumentException("You cannot edit a value which is not editable");
                        }

                        Object wrappedVal = null;

                        if (!field.getClass().isPrimitive() && !field.getClass().isInstance(String.class)) {
                            throw new IllegalArgumentException("Setting value is not primitive type or string which is not supported.");
                        }

                        if (boolean.class.equals(field.getType())) {
                            wrappedVal = Boolean.parseBoolean(val);
                        } else if (int.class.equals(field.getType())) {
                            wrappedVal = Integer.parseInt(val);
                        } else if (long.class.equals(field.getType())) {
                            wrappedVal = Long.parseLong(val);
                        } else if (double.class.equals(field.getType())) {
                            wrappedVal = Double.parseDouble(val);
                        } else if (short.class.equals(field.getType())) {
                            wrappedVal = Short.parseShort(val);
                        } else if (String.class.equals(field.getType())) {
                            wrappedVal = val;
                        }

                        Validate.notNull(wrappedVal);

//                        if(field.getDeclaringClass().isInstance(val.getClass())) {
                        field.set(this, wrappedVal);
                        return;
//                        } else {
//                            throw new IllegalArgumentException("Value has to be ")
//                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such value named " + key + " found");
    }

    public void setValue(@NonNull String key, Object val) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();

                if (name.equalsIgnoreCase(key)) {
                    try {

                        if (!settingValue.editable()) {
                            throw new IllegalArgumentException("You cannot edit a value which is not editable");
                        }

//                        if(field.getDeclaringClass().isInstance(val.getClass())) {
                        field.set(this, val);
                        return;
//                        } else {
//                            throw new IllegalArgumentException("Value has to be ")
//                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such value named " + key + " found");
    }

    public Object getValue(@NonNull String key) {
        for (Field field : getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SettingValue.class)) {
                SettingValue settingValue = field.getAnnotation(SettingValue.class);

                String name = settingValue.name();

                if (name.equals("")) name = field.getName();

                if (name.equalsIgnoreCase(key)) {
                    try {
                        return field.get(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such value named " + key + " found");

//        switch (key.toLowerCase()) {
//            case "password":
//                return getPassword();
//            case "usemulticast":
//                return isUseMulticast();
//            case "passwordlogin":
//                return isPasswordRequiredForLogin();
//            default:
//                return null;
//        }
    }

    private static final Object[] emptyObject = new Object[0];


    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public @interface SettingValue {
        @NonNull String name() default "";

        boolean editable() default true;

        @NonNull String[] values() default {};
    }
}
