package host.ankh.LL1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 巴克斯范式实现类(Backus Normal Form, BNF)
 * @author ankh
 * @created at 2022-04-22 21:55
 */
public class BNF {
    public static void main(String[] args) {
        String[] test = {"D::=*FD|ε", "T::=FD", "E::=TC", "F::=(E)|i", "C::=+TC|ε"};
        BNF bnf = new BNF(test);
        System.out.println(bnf.vnSet.toString());
        System.out.println(bnf.vtSet.toString());
        System.out.println(bnf.prodMap.toString());
    }
    // 产生式数组
    String[] grammarText;
    // 所有非终结符的集合
    HashSet<String> vnSet = new HashSet<>();
    // 所有终结符的集合
    HashSet<String> vtSet = new HashSet<>();

    // 非终结符对应的产生式映射
    Map<String, String[]> prodMap = new HashMap<>();

    BNF(String[] text) {
        grammarText = text;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 该初始化函数有两个功能
     *      1. 从 GrammarText 中获取所有的终结符和非终结符
     *      2. 初始化 prodMap
     */
    public void init() throws Exception{
        // 先找到所有的非终结符
        // 根据巴克斯范式的要求, 左部一定都是非终结符
        // 且对于每个非终结符, 一定存在它的产生式, 因此只需要遍历每个产生式的左部, 就可以得到所有的非终结符
        String[] RHS = new String[grammarText.length];
        // 当前产生式的索引
        int cnt = 0;
        for (String prod : grammarText) {
            // 每个产生式至少由5个字符组成: A::=c
            if (prod.length() < 5) {
                throw new Exception("产生式至少由五个字符组成!");
            }
            char c1 = prod.charAt(1);
            char c2 = prod.charAt(2);
            char c3 = prod.charAt(3);
            for (int i = 1, len = prod.length() - 3; i < len; i++) {
                // 暴力方法匹配 ::= (如果使用KMP, 会不会有点大材小用?)
                if (c1 == ':' && c2 == ':' && c3 == '=') {
                    // 找到了 ::= 的位置
                    // 提取左部和右部
                    vnSet.add(prod.substring(0, i));
                    RHS[cnt++] = prod.substring(i + 3);

                    // 初始化 prodMap
                    String[] split = prod.substring(i + 3).split("\\|");
                    prodMap.put(prod.substring(0, i), split);
                    break;
                }
                // 调整 c1 c2 c3的位置
                c1 = c2;
                c2 = c3;
                c3 = prod.charAt(i + 3);
            }
        }
        // 遍历 RHS: 右部集合
        char c;
        for (String r : RHS) {
            for (int i = 0, len = r.length(); i < len; i++) {
                c = r.charAt(i);
                if (c != '|' && !vnSet.contains(String.valueOf(c))) {
                    // 如果当前符号既不是连接符"|" 也不是非终结符, 那么它一定是终结符
                    vtSet.add(String.valueOf(c));
                }
            }
        }
    }
}
