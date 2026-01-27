package org.chemtrovina.cmtmsys.security;

import org.chemtrovina.cmtmsys.model.enums.UserRole;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresRoles {
    UserRole[] value() default {};   // roles được phép
    boolean allowAll() default false; // nếu true thì ai cũng vào
}
