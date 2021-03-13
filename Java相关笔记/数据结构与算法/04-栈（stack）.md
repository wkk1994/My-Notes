# 栈（stack）

## 栈的定义

栈的特征是先进者后出，后进者先出。是一种操作受限的线性表，只允许在一端插入和删除。

![栈示例图](https://static001.geekbang.org/resource/image/3e/0b/3e20cca032c25168d3cc605fa7a53a0b.jpg)

## 为什么要存在栈这种数据结构

从功能上看，数组或者链表都可以替代栈，但是要知道的是，特定的数据结构是对特定场景的抽象，而且数组或链表暴露了太多操作接口，操作上太灵活，就意味着不可控性，自然也会容易出错。栈只暴露插入和删除操作简化操作。当某个数据集合只涉及在一端插入和删除数据，并且满足后进先出、先进后出的特性，就应该首选“栈”这种数据结构。

## 栈的实现

顺序栈：用数组实现的栈。

链式栈：用链表实现的栈.

在入栈和出栈过程中，只需要一两个临时变量存储空间，所以空间复杂度是 O(1)。不管是顺序栈还是链式栈，入栈、出栈只涉及栈顶个别数据的操作，所以时间复杂度都是 O(1)。

数组实现的栈代码示例：

```java
public class ArrayStack {

    /**
     * 栈中元素个数
     */
    private int size;

    /**
     * 数组长度
     */
    private int length;

    /**
     * 数组
     */
    private String[] items;

    public ArrayStack(int n) {
        items = new String[n];
        this.length = n;
        this.size = 0;
    }

    /**
     * 入栈操作
     * @param item
     * @return
     */
    public boolean push(String item) {
        if(size == length) {
            // 数组空间不够，需要扩展
            grow();
        }
        items[size] = item;
        size ++;
        return true;
    }

    /**
     * 出栈操作
     * @return
     */
    public String pop() {
        if(size == 0){
            return null;
        }
        String item = items[size - 1];
        size--;
        return item;
    }
}
```

链表实现的栈代码示例：

```java
public class LinkedStack {

    /**
     * 链表
     */
    private LinkedList<String> items;

    public LinkedStack() {
        items = new LinkedList<>();
    }

    /**
     * 入栈操作
     * @param item
     * @return
     */
    public boolean push(String item) {
        return items.add(item);

    }

    /**
     * 出栈操作
     * @return
     */
    public String pop() {
        if(items == null || items.size() == 0) {
            return null;
        }
        return items.removeLast();
    }
}
```

### 支持动态扩容的顺序栈

对于上面实现的顺序栈，是一个固定大小的栈，如果当栈满后就无法向其中添加数据了。尽管链式栈的大小不受限制，但是链式栈的节点需要额外存储next指针，内存消耗相对较多。

如何实现一个动态扩展的顺序栈？可以在数组空间不足时，重新申请一个更大的内存（可以是原来数组大小的2倍），将原来的数据拷贝过去。这时候的顺序栈的出栈的时间复杂度还是O(1)；但是入栈时，最优情况时间复杂度是O(1)，最差情况时间复杂度是O(N)。

入栈操作的平均情况下的时间复杂度用摊还分析法来分析：假设初始栈大小为k，则在插入k个数据时只需要一个 simple-push 操作就可以完成，在插入k+1个元素时，需要重新申请内存和搬移数据。但是，接下来的 K-1 次入栈操作，都不需要再重新申请内存和搬移数据，所以这 K-1 次入栈操作都只需要一个 simple-push 操作就可以完成。

![入栈时间复杂度均摊分析](https://static001.geekbang.org/resource/image/c9/bb/c936a39ad54a9fdf526e805dc18cf6bb.jpg)

可以看出来，这 K 次入栈操作，总共涉及了 K 个数据的搬移，以及 K 次 simple-push 操作。将 K 个数据搬移均摊到 K 次入栈操作，那每个入栈操作只需要一个数据搬移和一个 simple-push 操作。以此类推，入栈操作的均摊时间复杂度就为 O(1)。

通过这个例子的实战分析，也印证了前面讲到的，均摊时间复杂度一般都等于最好情况时间复杂度。

## 栈的应用

### 栈在函数调用中的应用

函数调用栈：操作系统给每个线程分配了一块独立的内存空间，这快空间被组织成“栈”这种结构，用来存储函数调用时的临时变量。每进入一个函数，就会将临时变量作为一个栈帧入栈，当被调用函数执行完成，返回之后，将这个函数对应的栈帧出栈。

示例：

```c
int main() {
   int a = 1; 
   int ret = 0;
   int res = 0;
   ret = add(3, 5);
   res = a + ret;
   printf("%d", res);
   reuturn 0;
}

int add(int x, int y) {
   int sum = 0;
   sum = x + y;
   return sum;
}
```

对于上面代码的执行过程对应的函数栈里出栈、入栈的操作情况如下图：

![函数栈执行情况](https://static001.geekbang.org/resource/image/17/1c/17b6c6711e8d60b61d65fb0df5559a1c.jpg)

### 栈在表达式求值中的应用

编译器如何利用栈来实现表达式求值：

编译器通过两个栈来实现表达式求值：一个保存操作数的栈，一个保存运算符的栈。从左向右遍历表达式，当遇到数字时直接压入操作数栈；当遇到运算符时，就与运算符的栈顶元素比较：如果比运算符栈顶元素的优先级高，就将当前运算符压入栈；如果比运算符栈顶元素的优先级低或者相同，从运算符栈中取栈顶运算符，从操作数栈的栈顶取 2 个操作数，然后进行计算，再把计算完的结果压入操作数栈，继续比较。

示例：

`3 + 5 * 8 - 6`表达式的计算过程如下：

![表达式求值过程](https://static001.geekbang.org/resource/image/bc/00/bc77c8d33375750f1700eb7778551600.jpg)

### 栈在括号匹配中的应用

借助栈来检查表达式中的括号是否匹配：比如，{[] ()[{}]}或[{()}([])]等都为合法格式，而{[}()]或[({)]为不合法的格式。可以用栈来保存未匹配的左括号，从左到右依次扫描字符串。当扫描到左括号时，则将其压入栈中；当扫描到右括号时，从栈顶取出一个左括号。如果能够匹配，比如“(”跟“)”匹配，“[”跟“]”匹配，“{”跟“}”匹配，则继续扫描剩下的字符串。如果扫描的过程中，遇到不能配对的右括号，或者栈中没有数据，则说明为非法格式。

```java
   private static Set<String> leftSet = new HashSet<>();
   static {
        leftSet.add("{");
        leftSet.add("[");
        leftSet.add("(");
    }

    /**
     * 栈在括号匹配中的应用
     * {[()[]} false
     * @param bracket
     * @return
     */
    public static boolean bracketMatch(String bracket) {
        if(bracket == null) {
            return false;
        }
        Stack<String> stringStack = new Stack<>();
        for (int i = 0; i < bracket.length(); i++) {
            if(leftSet.contains(String.valueOf(bracket.charAt(i)))) {
                stringStack.add(String.valueOf(bracket.charAt(i)));
            }else {
                if(stringStack.empty()) {
                   return false;
                }
                String pop = stringStack.pop();
                if(!checkFlag(pop, String.valueOf(bracket.charAt(i)))){
                    return false;
                }
            }
        }
        return stringStack.empty();
    }

    private static boolean checkFlag(String left, String right) {
        if("[".equals(left) && "]".equals(right)) {
            return true;
        }else if("{".equals(left) && "}".equals(right)) {
            return true;
        }else if("(".equals(left) && ")".equals(right)) {
            return true;
        }
        return false;
    }
```

### 使用栈实现浏览器的前进、后退

使用两个栈，X 和 Y，把首次浏览的页面依次压入栈 X，当点击后退按钮时，再依次从栈 X 中出栈，并将出栈的数据依次放入栈 Y。当点击前进按钮时，依次从栈 Y 中取出数据，放入栈 X 中。当栈 X 中没有数据时，那就说明没有页面可以继续后退浏览了。当栈 Y 中没有数据，那就说明没有页面可以点击前进按钮浏览了。

## 问题

* **为什么函数调用要用“栈”来保存临时变量呢？用其他数据结构不行吗？**

不一定非要用栈来保存临时变量，只不过如果这个函数调用符合后进先出的特性，用栈这种数据结构来实现，是最顺理成章的选择。

从调用函数进入被调用函数，对于数据来说，变化的是什么呢？是作用域。所以根本上，只要能保证每进入一个新的函数，都是一个新的作用域就可以。而要实现这个，用栈就非常方便。在进入被调用函数的时候，分配一段栈空间给这个函数的变量，在函数结束的时候，将栈顶复位，正好回到调用函数的作用域内。

* **为什么内存中的“栈”也叫“栈”？跟我们这里说的“栈”是不是一回事呢？**

虽然内存中的栈和数据结构的栈不是一回事，即内存中的栈是一段虚拟的内存空间，数据结构中的栈是一种抽象的数据类型，但是它们都有“栈”的特性——后进先出，所以都叫“栈”也无可厚非。