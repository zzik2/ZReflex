# ZReflex

ZReflex is a small Java utility library for advanced reflection and access-modifier tweaks in mixin-based environments for minecraft. It provides:

- A `ZReflectionTool` utility with convenient helpers to read/write fields, invoke methods, and instantiate classes (including private members) with assignable type resolution.
- A `ZEnumTool` utility for dynamically adding or creating Enum constants at runtime.
- A `@ModifyAccess` annotation plus a `ModifyAccessTransformer` mixin plugin that can adjust field/method access modifiers after mixin application.

## Features

- Unified reflection helpers for fields, methods, constructors, and modifier checks.
- Optional lookup APIs to avoid exceptions when members are missing.
- Dynamic Enum constant manipulation (add, create instances).
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

### Dynamic Enum manipulation

Add a simple Enum constant:

```java
Color yellow = ZEnumTool.addConstant(Color.class, "YELLOW");
// yellow.name() == "YELLOW", yellow.ordinal() == 3 (if 3 existed before)
```

Add an Enum constant with constructor parameters:

```java
// For enums like: enum Size { SMALL(10), MEDIUM(20), LARGE(30); Size(int value) {...} }
Size xxl = ZEnumTool.addConstant(Size.class, "XXL", new Class<?>[] { int.class }, 100);
```

Create an Enum instance without adding to `values()`:

```java
Color temp = ZEnumTool.createInstance(Color.class, "TEMP", 999);
// Color.values() remains unchanged
```

Add multiple constants at once:

```java
ZEnumTool.addConstants(Color.class, List.of(color1, color2, color3));
```

> **Note**: Enum manipulation relies on JVM internals and is not guaranteed to work in all environments.

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