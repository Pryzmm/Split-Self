package com.pryzmm.splitself.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.loader.api.LanguageAdapter;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(LanguageOptionsScreen.class)
public class LanguageOptionsScreenMixin {

    @Unique
    private List<String> supportedLanguageCodes = List.of("cs_cz", "de_de", "en_us", "es_ar", "es_cl", "es_ec", "es_es", "es_mx", "es_uy", "es_ve", "fr_ca", "fr_fr", "it_it", "nl_be", "nl_nl", "pl_pl", "pt_br", "ro_ro", "ru_ru", "tr_tr", "zh_cn");

    @Inject(method = "initBody", at = @At("TAIL"))
    private void modifyLanguageButtons(CallbackInfo ci) {
        try {
            LanguageOptionsScreen screen = (LanguageOptionsScreen) (Object) this;

            Field field = LanguageOptionsScreen.class.getDeclaredField("languageSelectionList");
            field.setAccessible(true);
            Object listWidget = field.get(screen);

            System.out.println("Found languageSelectionList: " + listWidget);
            System.out.println("Is EntryListWidget: " + (listWidget instanceof EntryListWidget));

            if (listWidget instanceof EntryListWidget) {
                System.out.println("Processing language list...");
                processLanguageList((EntryListWidget<?>) listWidget);
            } else {
                // Fallback to reflection approach
                processLanguageListReflection(listWidget);
            }

        } catch (Exception e) {
            System.err.println("Failed to modify language buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processLanguageList(EntryListWidget<?> listWidget) {
        System.out.println("Processing language list widget: " + listWidget.getClass());

        try {
            Field childrenField = findChildrenField(listWidget.getClass());
            childrenField.setAccessible(true);
            List<?> entries = (List<?>) childrenField.get(listWidget);

            for (Object entry : entries) {
                markIfSupported(entry);
            }
        } catch (Exception e) {
            processLanguageListReflection(listWidget);
        }
    }

    @Unique
    private void processLanguageListReflection(Object listWidget) {

        String[] possibleChildrenFields = {"children", "entries", "items", "elements"};

        for (String fieldName : possibleChildrenFields) {
            try {
                Field childrenField = findFieldInHierarchy(listWidget.getClass(), fieldName);
                if (childrenField != null) {
                    childrenField.setAccessible(true);
                    Object fieldValue = childrenField.get(listWidget);

                    if (fieldValue instanceof List) {
                        List<?> entries = (List<?>) fieldValue;
                        for (Object entry : entries) {
                            System.out.println("Entry: " + entry.getClass().getName());
                            markIfSupported(entry);
                        }
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private Field findChildrenField(Class<?> clazz) throws NoSuchFieldException {
        String[] possibleNames = {"children", "entries", "items"};

        for (String name : possibleNames) {
            Field field = findFieldInHierarchy(clazz, name);
            if (field != null) {
                return field;
            }
        }

        throw new NoSuchFieldException("No children field found");
    }

    private Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    private void markIfSupported(Object entry) {
        Field[] fields = entry.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(entry);
                if (field.getName().equals("languageCode") && value instanceof String) {
                    String langCode = (String) value;
                    if (supportedLanguageCodes.contains(langCode)) {
                        System.out.println("  -> SUPPORTED LANGUAGE FOUND: " + langCode);
                        markEntryAsSupported(entry, langCode);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @Unique
    private void markEntryAsSupported(Object entry, String languageCode) {
        System.out.println("Marking entry as supported: " + languageCode);

        try {
            Field[] fields = entry.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entry);

                if (field.getName().contains("languageDefinition") && value instanceof net.minecraft.text.Text) {
                    net.minecraft.text.Text originalText = (net.minecraft.text.Text) value;

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

                    System.out.println("Modified text using Unsafe");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to modify entry text: " + e.getMessage());
            e.printStackTrace();
        }
    }
}