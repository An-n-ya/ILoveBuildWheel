package host.ankh.OperatorGrammar;

import host.ankh.BNF;

import java.util.*;

/**
 * @author ankh
 * @created at 2022-04-30 20:22
 */
public class OG {
    public static void main(String[] args) {
        String[] test = {"E::=E+T|T", "T::=T*F|F", "F::=P^F|P", "P::=(E)|i"};
        OG og = new OG(test);
        System.out.println(og);
    }

    private Map<String, Boolean[]> map = new HashMap<>();
    private BNF bnf;
    private Stack<String> stack1 = new Stack<>();
    private Stack<String> stack2 = new Stack<>();

    OG(String[] text) {
        bnf = new BNF(text, "E");

        // 初始化map
        for (String vn : bnf.vnSet) {
            for (String vt : bnf.vtSet) {
                map.put(vn + "-" + vt, new Boolean[]{false, false});
            }
        }

        init();
    }

    @Override
    public String toString() {
        StringBuilder sbf = new StringBuilder();
        StringBuilder sbl = new StringBuilder();

        sbf.append("FIRSTVT:\n");
        sbf.append("\t");
        sbl.append("LASTVT:\n");
        sbl.append("\t");
        for (String vt : bnf.vtSet) {
            // 打印表头
            sbf.append(vt + "\t");
            sbl.append(vt + "\t");
        }
        sbf.append("\n");
        sbl.append("\n");

        for (String vn : bnf.vnSet) {
            sbf.append(vn + "\t");
            sbl.append(vn + "\t");
            for (String vt : bnf.vtSet) {
                Boolean[] bools = map.get(vn + "-" + vt);
                sbf.append((bools[0] ? 1 : 0) + "\t");
                sbl.append((bools[1] ? 1 : 0) + "\t");
            }
            sbf.append("\n");
            sbl.append("\n");
        }

        return sbl.toString() + sbf.toString();
    }

    private void init() {

        for (String vn : bnf.vnSet) {
            String[] prods = bnf.prodMap.get(vn);
            for (String prod : prods) {
                String first = prod.substring(0,1);
                String last = prod.substring(prod.length() - 1);
                boolean isFirstVn = bnf.vtSet.contains(first);
                boolean isLastVn = bnf.vtSet.contains(first);
                if (isFirstVn) insert(vn + "-" + first, true);
                if (isLastVn) insert(vn + "-" + last, false);

                if (prod.length() == 1) continue;
                String second = prod.substring(1,2);
                String lastButOne = prod.substring(prod.length() - 2, prod.length() - 1);
                if (bnf.vnSet.contains(first) && bnf.vtSet.contains(second)) {
                    // 如果第一个字符是非终结符, 且第二个字符是终结符
                    insert(vn + "-" + second, true);
                }

                if (bnf.vnSet.contains(last) && bnf.vtSet.contains(lastButOne)) {
                    // 如果最后一个字符是非终结符, 且倒数第二个字符是终结符
                    insert(vn + "-" + lastButOne, false);
                }
            }
        }

        while(!stack1.empty()) {
            // 取出栈顶
            String top = stack1.pop();
            String vvn = top.substring(0,1);
            String vvt = top.substring(2);
            for (String vn : bnf.vnSet) {
                String[] prods = bnf.prodMap.get(vn);
                for (String prod : prods) {
                    String first = prod.substring(0,1);
                    if (first.equals(vvn) && bnf.vnSet.contains(first)) {
                        // 如果第一个字符与 vvn 相同, 且也是非终结符
                        insert(vn + "-" + vvt, true);
                    }
                }
            }
        }

        while(!stack2.empty()) {
            // 取出栈顶
            String top = stack2.pop();
            String vvn = top.substring(0,1);
            String vvt = top.substring(2);
            for (String vn : bnf.vnSet) {
                String[] prods = bnf.prodMap.get(vn);
                for (String prod : prods) {
                    String last = prod.substring(prod.length() - 1);
                    if (last.equals(vvn) && bnf.vnSet.contains(last)) {
                        // 如果第一个字符与 vvn 相同, 且也是非终结符
                        insert(vn + "-" + vvt, false);
                    }
                }
            }
        }
    }

    /**
     *
     * @param pa
     * @param type 计算FIRSTVT时type为true, 否则为false
     */
    private void insert(String pa, Boolean type) {
        int ind = type ? 0 : 1;
        Boolean[] bool = map.get(pa);
        if (bool[ind]) return;

        bool[ind] = true;

        // 把p-a位置设置为true
        map.put(pa, bool);
        // 推入p-a, 供之后寻找其他VT集合元素使用
        if (type) {
            stack1.push(pa);
        } else {
            stack2.push(pa);
        }
    }


}
