package host.ankh.ADT.List.MyArrayList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author ankh
 * @created at 2022-04-14 20:10
 */
public class MyArrayList<T> implements Iterable<T>{
    // 默认数组大小为 10
    private static final int DEFAULT_CAPACITY = 10;

    // 当前已存入表中的元素个数
    private int theSize;
    // 存入元素 T 的数组
    private T[] theItems;

    public MyArrayList() {
        doClear();
    }

    private void doClear() {
        theSize = 0;
        // 将 theItems 初始化为 DEFAULT_CAPACITY 大小
        ensureCapacity(DEFAULT_CAPACITY);
    }

    // 清空数组, 在这里只考虑把 theSize 设置为零, 而不用真的清空数组, 这样可以加快速度
    public void clear() {
        doClear();
    }

    public int size() {
        return theSize;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    // 将数组大小缩减到元素个数大小
    public void trimToSize() {
        ensureCapacity(size());
    }

    // 获取 idx 位置的元素
    public T get(int idx) {
        // 对 idx 的范围做下判断
        if (idx < 0 || idx >= size())
            // 这是数组访问越界的错误(Java内置的)
            throw new ArrayIndexOutOfBoundsException();
        return theItems[idx];
    }

    // 设置 idx 位置的元素为 newVal
    // 并返回被更改之前的值
    public T set(int idx, T newVal) {
        // 对 idx 的范围做下判断
        if (idx < 0 || idx >= size())
            // 这是数组访问越界的错误(Java内置的)
            throw new ArrayIndexOutOfBoundsException();
        T old = theItems[idx];
        theItems[idx] = newVal;
        return old;
    }

    // 为 theItems 数组设置大小
    public void ensureCapacity(int newCapacity) {
        // 如果新设置的大小小于当前已存入表中的元素的个数, 如果直接改变会造成内存浪费, 因此在这里不进行任何操作, 直接返回
        if (newCapacity < theSize) return;

        // 保存原来的数组
        T[] old = theItems;
        // 创建新的数组
        theItems = (T[]) new Object[newCapacity];

        // 将老数组里面的元素放入新数组
        for (int i = 0, len = size(); i < len; i++) {
            theItems[i] = old[i];
        }
    }

    // 在末尾添加元素
    public void add(T x) {
        add(size(), x);
    }

    // 在 idx 位置添加元素
    public void add(int idx, T x) {
        // 添加的时候需要判断当前数组满了没有, 如果满了则需要扩容
        if (size() == theItems.length) {
            // 扩容
            ensureCapacity(size() * 2 + 1);
        }
        // 位置在 idx 之后的元素都需要往后移
        // 后移需要从后往前遍历
        for(int i = size(); i > idx; i--) {
            // 后移
            theItems[i] = theItems[i-1];
        }
        // 将 idx 位置的元素设置为 x
        theItems[idx] = x;
        // size 加一
        theSize++;
    }

    // 删除 idx 位置的元素
    // 并返回所删除的元素的值
    public T remove(int idx) {
        // 先判断下 idx 的范围是否正确
        if (idx < 0 || idx > size()) {
            throw new ArrayIndexOutOfBoundsException();
        }

        T removedItem = theItems[idx];

        // idx 后面的元素往前移动
        // 注意这次是从前往后遍历
        for (int i = idx; i >= size() - 1; i--) {
            // 前移
            theItems[i] = theItems[i+1];
        }
        // 这里只需要让theSize减一即可, 没必要把最后一个元素清空, 这样可以减少操作
        theSize--;

        return removedItem;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < size(); i++) {
            stringBuilder.append(theItems[i]);
            if (i != size() - 1) stringBuilder.append(",");
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }



    // 下面的内容是用来实现 Iterator 接口, 方便对表进行遍历
    @Override
    public Iterator<T> iterator() {
        return new ArrayListIterator();
    }

    private class ArrayListIterator implements Iterator<T> {
        // 这里使用的是 内部类, 这是为了使用外部类中关于 theSize 相关的参数
        // 记录当前遍历到的位置
        private int current = 0;

        @Override
        public boolean hasNext() {
            return current < size();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                // 如果没有下一个元素还在调用next, 则返回没有这样的元素的错误(Java内置错误)
                throw new NoSuchElementException();
            }

            // 返回当前元素
            // 然后 当前位置 加一
            return theItems[current++];
        }

        @Override
        public void remove() {
            // 如果想调用外部类的remove方法会有一个问题
            // 内部类和外部类都有 remove 方法, 直接写 remove 会使用内部类的方法
            // 如果想使用外部的remove , 则需要使用 MyArrayList.this 来获取外部类的实例
            MyArrayList.this.remove(--current);
        }
    }
}
