package host.ankh.OperatorGrammar;

import host.ankh.BNF;

import java.util.*;

/**
 * @author ankh
 * @created at 2022-04-30 20:22
 */
public class OG {
    public static void main(String[] args) {
        String[] test = {"E::=E+T|T", "T::=T*F|F", "F::=P^F|P", "P::=(E)|i", "S::=#E#"};
        OG og = new OG(test);
        System.out.println(og);
        boolean res = og.analyze("(i+i)*i#");
        System.out.println("res = " + res);
    }

    private Map<String, Boolean[]> map = new HashMap<>();
    private BNF bnf;
    private Stack<String> stack1 = new Stack<>();
    private Stack<String> stack2 = new Stack<>();
    private Map<String, String> priorityMap = new HashMap<>();

    OG(String[] text) {
        bnf = new BNF(text, "S");

        // 初始化map
        for (String vn : bnf.vnSet) {
            for (String vt : bnf.vtSet) {
                map.put(vn + "-" + vt, new Boolean[]{false, false});
            }
        }

        // 初始化 priorityMap 为 #
        for (String vt : bnf.vtSet) {
            for (String vvt : bnf.vtSet) {
                priorityMap.put(vt + "-" + vvt, " ");
            }
        }

        init();

        createPriorityTable();
    }

    private void createPriorityTable() {
        for (String vn : bnf.vnSet) {
            String[] prods = bnf.prodMap.get(vn);
            for (String prod : prods) {
                for (int i = 0; i < prod.length() - 1; i++) {
                    String first = String.valueOf(prod.charAt(i));
                    String second = String.valueOf(prod.charAt(i + 1));
                    if (i < prod.length() - 2) {
                        String last = String.valueOf(prod.charAt(i + 2));
                        case2(first, second, last);
                    }
                    case1(first, second);
                    case3(first, second);
                    case4(first, second);
                }
            }
        }
    }

    /**
     * 如果x_i 和 x_i+1 都是终结符
     * @param first
     * @param last
     */
    private void case1(String first, String last) {
        if (bnf.vtSet.contains(first) && bnf.vtSet.contains(last)) {
            priorityMap.put(first + "-" + last, "=");
        }
    }

    /**
     * 如果first和last都是终结符, 但是mid是非终结符
     * @param first
     * @param mid
     * @param last
     */
    private void case2(String first, String mid, String last) {
        if (bnf.vtSet.contains(first) && bnf.vnSet.contains(mid) && bnf.vtSet.contains(last)) {
            priorityMap.put(first + "-" + last, "=");
        }
    }

    /**
     * 如果first是终结符, 而last是非终结符
     * @param first
     * @param last
     */
    private void case3(String first, String last) {
        if(!(bnf.vtSet.contains(first) && bnf.vnSet.contains(last))) return;
        for(String vt : bnf.vtSet) {
            Boolean[] bools = map.get(last + "-" + vt);
            if (bools[0]) {
                priorityMap.put(first + "-" + vt, "<");
            }
        }
    }

    /**
     * 如果last是终结符, 而frist是非终结符
     * @param first
     * @param last
     */
    private void case4(String first, String last) {
        if(!(bnf.vnSet.contains(first) && bnf.vtSet.contains(last))) return;
        for (String vt : bnf.vtSet) {
            Boolean[] bools = map.get(first + "-" + vt);
            if (bools[1]) {
                priorityMap.put(vt + "-" + last, ">");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sbf = new StringBuilder();
        StringBuilder sbl = new StringBuilder();
        StringBuilder sbp = new StringBuilder();

        sbf.append("FIRSTVT:\n");
        sbf.append("\t");
        sbl.append("LASTVT:\n");
        sbl.append("\t");
        sbp.append("PriorityTable:\n");
        sbp.append("\t");
        for (String vt : bnf.vtSet) {
            // 打印表头
            sbf.append(vt + "\t");
            sbl.append(vt + "\t");
            sbp.append(vt + "\t");
        }
        sbf.append("\n");
        sbl.append("\n");
        sbp.append("\n");

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

        for (String vt : bnf.vtSet) {
            sbp.append(vt + "\t");
            for (String vvt : bnf.vtSet) {
                String s = priorityMap.get(vt + "-" + vvt);
                sbp.append(s + "\t");
            }
            sbp.append("\n");
        }

        return sbl.toString() + sbf.toString() + sbp.toString();
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

    public boolean analyze(String in) {
        ArrayList<String> s = new ArrayList<>();
        s.add("#");
        int k = 0;
        int j;
        int t = 0;
        while (true) {
            String c = String.valueOf(in.charAt(t++));
            if (bnf.vtSet.contains(s.get(k))) {
                j = k;
            } else {
                j = k - 1;
            }
            
            while (biggerThan(s.get(j), c)) {
                String Q;
                do {
                    Q = s.get(j);
                    if (bnf.vtSet.contains(s.get(j - 1))) {
                        j = j - 1;
                    } else {
                        j = j - 2;
                    }
                } while(!(smallerThan(s.get(j), Q)));
                
                // reduce
                reduce(j + 1, k, s);

                k = j + 1;
            }

            if (smallerThan(s.get(j), c) || equalTo(s.get(j), c)) {
                k = k + 1;
                s.add(c);
            } else {
                System.out.println("错误!");
            }
            if (c.equals("#")) {
                break;
            }
        }
        return true;
    }
    
    private void reduce(int start, int end, ArrayList<String> s) {
        List<String> sub = s.subList(start, end + 1);
        boolean stop = false;
        for(String vn : bnf.vnSet) {
            String[] prods = bnf.prodMap.get(vn);
            for (String prod : prods) {
                boolean same = true;
                if (sub.size() != prod.length()) {
                    continue;
                }
                for (int i = 0, len = prod.length(); i < len; i++) {
                    String c1 = String.valueOf(prod.charAt(i));
                    String c2 = sub.get(i);
                    if ((bnf.vnSet.contains(c1) && bnf.vnSet.contains(c2)) || (bnf.vtSet.contains(c1) && bnf.vtSet.contains(c2) && c1.equals(c2))) {
                        continue;
                    } else {
                        same = false;
                        break;
                    }
                }
                // 对比每个产生式
                if (same) {
                    sub.clear();
                    s.add(start, vn);
                    stop = true;
                    break;
                }
            }
            if (stop) {
                break;
            }
        }
    }
    
    private boolean biggerThan(String a, String b) {
        if (priorityMap.get(a + "-" + b) == ">") {
            return true;
        } else {
            return false;
        }
    }
    private boolean smallerThan(String a, String b) {
        if (priorityMap.get(a + "-" + b) == "<") {
            return true;
        } else {
            return false;
        }
    }

    private boolean equalTo(String a, String b) {
        if (priorityMap.get(a + "-" + b) == "=") {
            return true;
        } else {
            return false;
        }
    }
    

}
