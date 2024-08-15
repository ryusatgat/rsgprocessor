# RSGProcessor
Java Operator Overloading for BigDecimal

## How to build
```sh
mvn package
```

## Test Example
```java
import java.math.BigDecimal;
import com.ryusatgat.processor.RSGBigDecimal;

public class Main {
  @RSGBigDecimal
  public static void main(String[] args) {
    BigDecimal a = 1 + 2;
    System.out.println(a + 1.1);
  }
}
```

## How to use java processor
```sh
javac -processor com.ryusatgat.processor.RSGProcessor \
      -J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
      -J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
      -J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
      -cp rsg-processor-x.x.jar Main.java
```

## Converted code by RSGProcessor
```java
import java.math.BigDecimal;

public class Main {
  public static void main(String[] paramArrayOfString) {
    BigDecimal bigDecimal = (
      new BigDecimal("1")).add(new BigDecimal("2"));
    System.out.println(bigDecimal.add(new BigDecimal("1.1")));
  }
}
```
