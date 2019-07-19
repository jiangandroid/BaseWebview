package com.jiang.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.jiang.annotations.JsRegister;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by xiyou on 2019/4/8
 */
@AutoService(Processor.class)
public class JsMethodRegisterProcessor extends AbstractProcessor {
    Messager messager;
    Elements elementUtils;
    Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    /**
     * 指定该注解处理器可以解决的类型，需要完整的包名+类命
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationSet = new LinkedHashSet<>();
        annotationSet.add(JsRegister.class.getCanonicalName());
        return annotationSet;
    }

    /**
     * 指定编译的JDK版本
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 处理注解的process函数
     *
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set annotations, RoundEnvironment roundEnv) {
        //KEY:方法名methodName，value：全类名fullClassName
        Map<String, String> registerMap = new HashMap<>();

        //注解应用全类名--注解list
        Map<String, List<ElementBean>> annotatedElementMap = new HashMap<>();
        //OnceClick.class 以 @Target(ElementType.METHOD)修饰
        for (Element element : roundEnv.getElementsAnnotatedWith(JsRegister.class)) {
            ElementBean randomElement = new ElementBean(element);
            messager.printMessage(Diagnostic.Kind.NOTE, randomElement.toString());

            //按被注解元素所在类的完整类名为key将被注解元素存储进Map中，后面会根据key生成类文件
            String qualifier = randomElement.fullClassName;
            if (annotatedElementMap.get(qualifier) == null) {
                annotatedElementMap.put(qualifier, new ArrayList<ElementBean>());
            }
            annotatedElementMap.get(qualifier).add(randomElement);

            messager.printMessage(Diagnostic.Kind.NOTE, randomElement.packageName + "/" + randomElement.className + "/" + randomElement.methodName);
        }
        for (Map.Entry<String, List<ElementBean>> entry : annotatedElementMap.entrySet()) {
            entry.getKey();
            try {
                new JsMethodWriter(entry.getKey(), entry.getValue())
                        .generate()
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    class JsMethodWriter {
        //注解所在的类全类名
        String fullClassName;
        String packageName;
        //改类中注解标记的列表
        List<ElementBean> elementBeans;//需要注册的js---class键值对
        final String paramName = "handlerRegistryMap";//register方法参数名

        private String getPackage() {
            return fullClassName.substring(0, fullClassName.lastIndexOf("."));
        }

        private String getClassName() {
            return fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        }

        private JsMethodWriter(String fullClassName, List<ElementBean> elementBeans) {
            this.fullClassName = fullClassName;
            this.elementBeans = elementBeans;
        }

        private JavaFile generate() {
            return JavaFile.builder(getPackage(), createType())
                    .addFileComment("Generated code. Do not modify!")
                    .build();
        }

        private TypeSpec createType() {
            return TypeSpec.classBuilder(getClassName() + "$$YtJsMethodRegister")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get("com.jiang.baseWebview", "YtJsMethodRegister"))
                    .addMethod(createRegisterMethod())
                    .build();
        }

        private MethodSpec createRegisterMethod() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("register")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterizedTypeName.get(Map.class, String.class, String.class), paramName);
            for (ElementBean elementBean : elementBeans) {
                try {
                    builder.addStatement(paramName + ".put($S, $S)", elementBean.methodName, fullClassName);
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                }
            }
            return builder.build();
        }
    }

    class ElementBean {
        String fullClassName;//全类名
        String className;//类名
        String packageName;//包名
        String methodName;//方法名

        ElementBean(Element element) {
            //对于Element直接强转
            ExecutableElement executableElement = (ExecutableElement) element;
            //非对应的Element，通过getEnclosingElement转换获取
            TypeElement classElement = (TypeElement) element.getEnclosingElement();

            //当(ExecutableElement) element成立时，
            // 使用(PackageElement) element.getEnclosingElement();将报错。
            //需要使用elementUtils来获取

            PackageElement packageElement = elementUtils.getPackageOf(classElement);
            fullClassName = classElement.getQualifiedName().toString();
            className = classElement.getSimpleName().toString();
            packageName = packageElement.getQualifiedName().toString();
            methodName = executableElement.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, packageName + "/" + className + "/" + methodName);
//            //取得方法参数列表
//            List<? extends VariableElement> methodParameters = executableElement.getParameters();
//            //参数类型列表
//            List<String> types = new ArrayList<>();
//            for (VariableElement variableElement : methodParameters) {
//                TypeMirror methodParameterType = variableElement.asType();
//                if (methodParameterType instanceof TypeVariable) {
//                    TypeVariable typeVariable = (TypeVariable) methodParameterType;
//                    methodParameterType = typeVariable.getUpperBound();
//
//                }
//                //参数名
//                String parameterName = variableElement.getSimpleName().toString();
//                //参数类型
//                String parameteKind = methodParameterType.toString();
//                types.add(methodParameterType.toString());
//            }
        }
    }
}
