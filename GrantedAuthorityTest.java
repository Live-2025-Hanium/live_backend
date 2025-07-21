import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class GrantedAuthorityTest {
    public static void main(String[] args) {
        // 람다로 구현한 GrantedAuthority (현재 JwtFilter 방식)
        GrantedAuthority lambda1 = () -> "ROLE_USER";
        GrantedAuthority lambda2 = () -> "ROLE_USER";
        
        // SimpleGrantedAuthority (권장 방식)
        GrantedAuthority simple1 = new SimpleGrantedAuthority("ROLE_USER");
        GrantedAuthority simple2 = new SimpleGrantedAuthority("ROLE_USER");
        
        System.out.println("=== 람다 방식 (문제 있음) ===");
        System.out.println("lambda1.equals(lambda2): " + lambda1.equals(lambda2)); // false!
        System.out.println("lambda1.hashCode(): " + lambda1.hashCode());
        System.out.println("lambda2.hashCode(): " + lambda2.hashCode());
        
        System.out.println("\n=== SimpleGrantedAuthority (올바름) ===");
        System.out.println("simple1.equals(simple2): " + simple1.equals(simple2)); // true!
        System.out.println("simple1.hashCode(): " + simple1.hashCode());
        System.out.println("simple2.hashCode(): " + simple2.hashCode());
        
        System.out.println("\n=== 크로스 비교 ===");
        System.out.println("lambda1.equals(simple1): " + lambda1.equals(simple1)); // false!
        System.out.println("simple1.equals(lambda1): " + simple1.equals(lambda1)); // false!
    }
} 