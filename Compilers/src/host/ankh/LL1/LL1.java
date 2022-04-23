package host.ankh.LL1;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ankh
 * @created at 2022-04-22 22:43
 */
public class LL1 {
    public static void main(String[] args) {
        String[] test = {"D::=*FD|ε", "T::=FD", "E::=TC", "F::=(E)|i", "C::=+TC|ε"};
        LL1 l = new LL1(test);
        System.out.println(l);

        try {
            l.analyze("i+i*i#");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Map<String, HashSet<String>> firstMap = new HashMap<>();
    Map<String, HashSet<String>> followMap = new HashMap<>();
    // 分析表
    Map<String, HashSet<String>> selectMap = new HashMap<>();
    BNF bnf;

    LL1(String[] text) {
        bnf = new BNF(text, "E");
        init();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bnf.toString()+ "\n");
        for (String s : bnf.vnSet) {
            sb.append(
                    "FIRST(" + s + ")=" + firstMap.get(s).toString() + "                  "
            );

            sb.append(
                    "FOLLOW(" + s + ")=" + followMap.get(s).toString() + "\n"
            );
        }

        // 打印表头
        for (String s : bnf.vtSet) {
            if (s.equals("ε")) continue;
            sb.append("\t" + s);
        }
        sb.append("\t#\n");

        for (String vn : bnf.vnSet) {
            // 表头
            sb.append(vn + "\t");
            for (String vt : bnf.vtSet) {
                if (vt.equals("ε")) continue;
                String[] set = selectMap.get(vn + "-" + vt).toArray(new String[0]);
                if (set.length > 0) {
                    sb.append(set[0] + "\t");
                } else {
                    sb.append(" " + "\t");
                }
            }
            String[] set = selectMap.get(vn + "-#").toArray(new String[0]);
            if (set.length > 0) {
                sb.append(set[0] + "\n");
            } else {
                sb.append(" " + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * 初始化 FIRST 集合 和 FOLLOW 集合
     */
    public void init() {
        try {
            calcFirst();
            calcFollow();
            calcSelect();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用 LL1 文法进行分析
     */
    public void analyze(String text) throws Exception{
        // 文法处理栈
        ArrayDeque<String> stack = new ArrayDeque<>();
        stack.push("#");
        // 起始的 cur 是文法的开始符号
        stack.push(bnf.start);
        boolean flag = true;
        // 当前文法单位
        String cur;
        // 处理 text 的当前指针
        int i = 0;
        while (flag) {
            cur = stack.pop();
            // 当前正在分析的字符
            String cText = String.valueOf(text.charAt(i));
            if (bnf.vtSet.contains(cur)) {
                // 如果当前符号是终结符
                if (cur.equals(cText)) i = i + 1;
            } else if (cur.equals("#") && cText.equals("#")) {
                // 如果都是终止符号, 说明分析成功
                // 退出循环
                flag = false;
            } else if (selectMap.get(cur + "-" + cText).size() >= 1) {
                // 如果 分析表中有数据
                String[] selectSet = selectMap.get(cur + "-" + cText).toArray(new String[0]);
                if (selectSet.length > 1) {
                        System.out.println("Warning! 出现语法二义性, 当" + cur + "遇到" + cText + "时, 会被推导为" + selectSet[0]);
                }
                for (int j = selectSet[0].length() - 1; j >= 0; j--) {
                    // 倒序放入 stack
                    String ss = String.valueOf(selectSet[0].charAt(j));
                    if (!ss.equals("ε")) {
                        stack.push(ss);
                    }
                }
            } else {
                // 其他情况都是错误
                throw new Exception("解析错误");
            }
        }

        System.out.println("解析成功!");
    }

    public void calcSelect() throws Exception {
        // 初始化 selectMap
        for (String vn : bnf.vnSet) {
            for (String vt : bnf.vtSet) {
                selectMap.put(vn + "-" + vt, new HashSet<>());
            }

            // 终止符 也有对应的 select 表
            selectMap.put(vn + "-#", new HashSet<>());
        }

        for (String vn : bnf.vnSet) {
            String[] prods = bnf.prodMap.get(vn);

            for (String prod : prods) {
                String[] first = findFirstWord(prod);
                for (int i = 0, len = first.length; i < len; i++) {
                    selectMap.get(vn + "-" + first[i]).add(prod);
                    if (first[i].equals("ε")) {
                        // 如果 first 里有空
                        // 将 vn 的 FOLLOW 集合里的元素放入分析表
                        HashSet<String> followSet = followMap.get(vn);
                        for (String s : followSet) {
                            selectMap.get(vn + "-" + s).add(prod);
                        }
                    }
                }
            }
        }
    }

    private String[] findFirstWord(String in) throws Exception {
        if (in.length() < 1) throw new Exception("产生式不能为空");
        String f = String.valueOf(in.charAt(0));
        if (bnf.vnSet.contains(f)) {
            // 如果是非终结符
            // 返回FIRST集合
            HashSet<String> set = firstMap.get(f);
            return firstMap.get(f).toArray(new String[0]);
        } else {
            // 如果是终结符, 直接返回本身
            String[] res = new String[1];
            res[0] = f;
            return res;
        }
    }

    public void calcFirst() {
        while (true) {
            AtomicBoolean whileContinue = new AtomicBoolean(false);
            for (String vn : bnf.vnSet) {
                // 遍历每一个非终结符, 计算它们的FIRST集合

                // 如果还没创建对应的集合则进行创建
                if (!firstMap.containsKey(vn)) {
                    firstMap.put(vn, new HashSet<>());
                }

                // 拿到该非终结符对应的产生式
                String[] prods = bnf.prodMap.get(vn);
                for (String prod : prods) {
                    AtomicBoolean loopContinue = new AtomicBoolean(false);
                    // 遍历每种可能的产生式
                    for (int i = 0, len = prod.length(); i < len; i++) {
                        // 取产生式符号串的第一个字符
                        String x1 = String.valueOf(prod.charAt(i));
                        if (bnf.vnSet.contains(x1)) {
                            // 如果字符是非终结符, 将该非终结符的 FIRST 集合的元素加入
                            if (!firstMap.containsKey(x1)) {
                                // 如果该字符的 FIRST 不存在, 创建新的
                                firstMap.put(x1, new HashSet<>());
                                // 终止循环
                                loopContinue.set(false);
                            } else {
                                firstMap.get(x1).forEach(s -> {
                                    if (s != "ε") {
                                        // 把 ε 以外的元素加入
                                        HashSet<String> firstSet = firstMap.get(vn);
                                        // 如果不存在才加入
                                        if (!firstSet.contains(s)) {
                                            firstSet.add(s);
                                            whileContinue.set(true);
                                        }
                                    } else {
                                        // 如果存在 ε 的元素, 继续循环
                                        loopContinue.set(true);
                                    }
                                });
                            }
                        } else {
                            // 如果是终结符, 直接加入 FIRST 集合, 并终止for循环
                            HashSet<String> firstSet = firstMap.get(vn);
                            // 如果不存在才加入
                            if (!firstSet.contains(x1)) {
                                firstSet.add(x1);
                                whileContinue.set(true);
                            }
                            loopContinue.set(false);
                        }
                        if (!loopContinue.get()) {
                            break;
                        }

                    }
                }
            }

            if (!whileContinue.get()) {
                break;
            }
        }
    }

    public void calcFollow() {
        // 为每个非终结符创建 follow set
        for (String s: bnf.vnSet) {
            followMap.put(s, new HashSet<>());
            if (s.equals(bnf.start)) {
                // 如果 s 是开始符号, 则把终止符 # 放入它的 FOLLOW 集合中
                followMap.get(s).add("#");
            }
        }
        while (true) {
            boolean whileContinue = false;

            for (String vn : bnf.vnSet) {
                // 拿到每个终结符对应的产生式
                String[] prods = bnf.prodMap.get(vn);
                // 对每个产生式进行处理
                for (String prod : prods) {
                    for (int i = 0, len = prod.length(); i < len; i++) {
                        // vn ::= \alpha x1 \beta
                        String x1 = String.valueOf(prod.charAt(i));
                        if (bnf.vnSet.contains(x1)) {
                            String beta = i < len - 1 ? String.valueOf(prod.charAt(i + 1)) : null;
                            if (beta == null || (bnf.vnSet.contains(beta) && includeEmpty(firstMap.get(beta)))) {
                                // i 来到了最后的位置 或者是 beta 中含有空
                                // 将vn的FOllOW加入 x1 的FOLLOW集合
                                HashSet<String> vnSet = followMap.get(vn);
                                HashSet<String> x1Set = followMap.get(x1);
                                for (String s : vnSet) {
                                    if(!x1Set.contains(s)) {
                                        // 只有不存在时才加入
                                        x1Set.add(s);
                                        whileContinue = true;
                                    }
                                }

                            }
                            if (beta != null) {
                                // 如果 beta 不为空, 把 beta 的 FIRST 集合加入 x1 中
                                HashSet<String> x1Set = followMap.get(x1);
                                if (bnf.vnSet.contains(beta)) {
                                    // 如果 beta 是非终结符, 将 beta 的 FIRST 集合中的非空元素加入
                                    HashSet<String> betaFirst = firstMap.get(beta);
                                    for (String s : betaFirst) {
                                        if ((!x1Set.contains(s)) &&  !s.equals("ε")) {
                                            x1Set.add(s);
                                            whileContinue = true;
                                        }
                                    }
                                } else {
                                    // 如果 beta 是终结符, 直接加入即可
                                    if (!x1Set.contains(beta)) {
                                        // 不拥有时加入
                                        x1Set.add(beta);
                                        whileContinue = true;
                                    }
                                }
                            }
                        } else {
                            // 非终结符没有 follow 集合
                            continue;
                        }
                    }
                }
            }

            if (!whileContinue) {
                break;
            }
        }
    }

    private boolean includeEmpty(HashSet<String> set) {
        return set.contains("ε");
    }
}
