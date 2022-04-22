package host.ankh.ADT.List.test;

import host.ankh.ADT.List.MyArrayList.MyArrayList;

/**
 * @author ankh
 * @created at 2022-04-14 20:56
 */
public class TestArrayList {
    public static void main(String[] args) {
        MyArrayList<Integer> list = new MyArrayList<>();

        // list 初始化为 1 2 3
        list.add(1);
        list.add(2);
        list.add(3);
        System.out.println("list = " + list);

        // 测试删除
        list.remove(2);
        System.out.println("list = " + list);

        // 测试末尾添加
        list.add(4);
        System.out.println("list = " + list);

        // 测试插入
        list.add(1, 1);
        System.out.println("list = " + list);

        // 测试遍历
        for (Integer i : list) {
            System.out.println("i = " + i);
        }

        for (int i = 0, len = list.size(); i < len; i++) {
            System.out.println("i = " + list.get(i));
        }

        // 测试修改
        for (int i = 0, len = list.size(); i < len; i++) {
            // 将所有位置的值改为1
            list.set(i, 1);
        }
        System.out.println("list = " + list);

    }

}
