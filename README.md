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

public class Rs {
  @RSGBigDecimal  // @RSGBigDecimal("OP2") you can change keyword
  public static void main(String[] args) {
    BigDecimal a = OP(1 + 2);
    BigDecimal b = OP(3);
    BigDecimal c = OP(a + b);
    

    System.out.println(OP(a + 1.1));
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

## Generated class by RSGProcessor (disassembled)
```java
import java.math.BigDecimal;

public class Rs {
  public static void main(String[] paramArrayOfString) {
    BigDecimal bigDecimal1 = (
      new BigDecimal("1")).add(new BigDecimal("2"));
    BigDecimal bigDecimal2 = new BigDecimal("3");
    BigDecimal bigDecimal3 = bigDecimal1.add(bigDecimal2);
    System.out.println(bigDecimal1.add(new BigDecimal("1.1")));
  }
}
```
