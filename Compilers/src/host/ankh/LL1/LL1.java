package host.ankh.LL1;

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
    }

    Map<String, HashSet<String>> firstMap = new HashMap<>();
    Map<String, HashSet<String>> followMap = new HashMap<>();
    BNF bnf;

    LL1(String[] text) {
        bnf = new BNF(text);
        init();
    }

    /**
     * 初始化 FIRST 集合 和 FOLLOW 集合
     */
    public void init() {
        calcFirst();
        calcFollow();
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

    }
}
