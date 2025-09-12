package com.pryzmm.splitself.mixin;

import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Mixin(LanguageOptionsScreen.class)
public class LanguageOptionsScreenMixin {

    @Unique
    private List<String> supportedLanguageCodes = List.of("cs_cz", "en_us", "es_ar", "es_cl", "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "fr_ca", "hu_hu", "it_it", "nl_be", "nl_nl", "pl_pl", "pt_br", "ro_ro", "ru_ru", "sv_se", "tr_tr", "zh_cn");

    @Inject(method = "initBody", at = @At("TAIL"))
    private void modifyLanguageButtons(CallbackInfo ci) {
        try {
            LanguageOptionsScreenMixin screen = this;
            EntryListWidget<?> listWidget = findEntryListWidget(screen);

            if (listWidget != null) {
                System.out.println("Found languageSelectionList via type search: " + listWidget);
                processLanguageList(listWidget);
            } else {
                System.err.println("Could not find language selection list widget");
            }

        } catch (Exception e) {
            System.err.println("Failed to modify language buttons: " + e.getMessage());
        }
    }

    @Unique
    private EntryListWidget<?> findEntryListWidget(LanguageOptionsScreenMixin screen) {
        Class<?> screenClass = screen.getClass();

        while (screenClass != null && screenClass != Object.class) {
            for (Field field : screenClass.getDeclaredFields()) {
                if (EntryListWidget.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        return (EntryListWidget<?>) field.get(screen);
                    } catch (Exception ignored) {}
                }
            }
            screenClass = screenClass.getSuperclass();
        }

        return null;
    }

    @Unique
    private void processLanguageList(EntryListWidget<?> listWidget) {
        System.out.println("Processing language list widget: " + listWidget.getClass());

        try {
            List<?> entries = findEntriesList(listWidget);

            if (entries != null) {
                for (Object entry : entries) {
                    markIfSupported(entry);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Unique
    private List<?> findEntriesList(Object listWidget) {
        Class<?> currentClass = listWidget.getClass();

        String[] possibleFieldNames = {"children", "entries", "items", "elements", "list"};

        while (currentClass != null && currentClass != Object.class) {
            for (String fieldName : possibleFieldNames) {
                try {
                    Field field = currentClass.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(listWidget);
                    if (value instanceof List<?>) {
                        return (List<?>) value;
                    }
                } catch (Exception ignored) {}
            }

            for (Field field : currentClass.getDeclaredFields()) {
                try {
                    if (List.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        Object value = field.get(listWidget);
                        if (value instanceof List<?> list && !list.isEmpty()) {
                            return list;
                        }
                    }
                } catch (Exception ignored) {}
            }
            currentClass = currentClass.getSuperclass();
        }

        return null;
    }

    @Unique
    private void markIfSupported(Object entry) {
        System.out.println("Examining entry: " + entry.getClass().getName());

        Field[] fields = getAllFields(entry.getClass());

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(entry);
                System.out.println("  Field: " + field.getName() + " (" + field.getType().getSimpleName() + ") = " +
                        (value != null ? value.toString() : "null"));

                if ((field.getName().toLowerCase().contains("language") && field.getName().toLowerCase().contains("code")) ||
                        field.getName().equals("languageCode") ||
                        (value instanceof String && isLanguageCode((String) value))) {

                    String langCode = (String) value;
                    if (supportedLanguageCodes.contains(langCode)) {
                        System.out.println("  -> SUPPORTED LANGUAGE FOUND: " + langCode);
                        markEntryAsSupported(entry, langCode);
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("  Field: " + field.getName() + " = <error: " + e.getMessage() + ">");
            }
        }
    }

    @Unique
    private Field[] getAllFields(Class<?> clazz) {
        java.util.ArrayList<Field> allFields = new java.util.ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            allFields.addAll(Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        }

        return allFields.toArray(new Field[0]);
    }

    @Unique
    private boolean isLanguageCode(String value) {
        return value != null && value.matches("^[a-z]{2}_[a-z]{2}$");
    }

    @Unique
    private void markEntryAsSupported(Object entry, String languageCode) {
        System.out.println("Marking entry as supported: " + languageCode);

        try {
            Field[] fields = getAllFields(entry.getClass());

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entry);

                if ((field.getName().toLowerCase().contains("language") &&
                        field.getName().toLowerCase().contains("definition")) ||
                        field.getName().contains("languageDefinition") ||
                        field.getName().toLowerCase().contains("text") ||
                        (value instanceof net.minecraft.text.Text)) {

                    if (value instanceof net.minecraft.text.Text originalText) {
                        // Create modified text with gold color
                        net.minecraft.text.Text modifiedText = net.minecraft.text.Text.literal("")
                                .append(net.minecraft.text.Text.literal("★ ").formatted(net.minecraft.util.Formatting.GOLD))
                                .append(originalText);

                        // Use Unsafe to modify the final field
                        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                        unsafeField.setAccessible(true);
                        Unsafe unsafe = (Unsafe) unsafeField.get(null);

                        long offset = unsafe.objectFieldOffset(field);
                        unsafe.putObject(entry, offset, modifiedText);

                        System.out.println("Modified text field '" + field.getName() + "' using Unsafe");
                        return;
                    }
                }
            }
            System.out.println("No suitable text field found for modification");
        } catch (Exception e) {
            System.out.println("Failed to modify entry text: " + e.getMessage());
        }
    }
}