# ZReflex

ZReflex is a small Java utility library for advanced reflection and access-modifier tweaks in mixin-based environments for minecraft. It provides:

- A `ZReflectionTool` utility with convenient helpers to read/write fields, invoke methods, and instantiate classes (including private members) with assignable type resolution.
- A `@ModifyAccess` annotation plus a `ModifyAccessTransformer` mixin plugin that can adjust field/method access modifiers after mixin application.

## Features

- Unified reflection helpers for fields, methods, constructors, and modifier checks.
- Optional lookup APIs to avoid exceptions when members are missing.
- Mixin plugin that rewrites access flags based on `@ModifyAccess` annotations.

## Installation (Gradle)

```gradle
repositories {
    mavenCentral()
}

dependencies {
    implementation "kr.zzik2:zreflex:0.0.1"
}
```

## Usage

### Reflection helpers

```java
String value = ZReflectionTool.getFieldValue(target, "privateField");
ZReflectionTool.setFieldValue(target, "privateField", "modified");
String result = ZReflectionTool.invokeMethod(target, "methodName", arg1, arg2);
MyType instance = ZReflectionTool.newInstance(MyType.class, "arg");
```

### Mixin access modifier changes

1. Register the plugin in your `mixin.json`:

```json
{
  "plugin": "zzik2.zreflex.mixin.ModifyAccessTransformer"
}
```

2. Annotate target members in your mixin class:

```java
@ModifyAccess(access = { Opcodes.ACC_PUBLIC }, removeFinal = true)
@Shadow @Final private int someField;
```

## Requirements

- Java 11+

---