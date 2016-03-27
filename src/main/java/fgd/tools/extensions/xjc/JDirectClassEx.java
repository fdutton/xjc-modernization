package fgd.tools.extensions.xjc;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JPackage;
import com.sun.codemodel.internal.JTypeVar;

public final class JDirectClassEx extends JClass {

    private final String fullName;

    public JDirectClassEx(JCodeModel _owner,String fullName) {
        super(_owner);
        this.fullName = fullName;
    }

    @Override
    public String name() {
        int i = fullName.lastIndexOf('.');
        if(i>=0)    return fullName.substring(i+1);
        return fullName;
    }

    @Override
    public String fullName() {
        return fullName;
    }

    @Override
    public JPackage _package() {
        int i = fullName.lastIndexOf('.');
        if(i>=0)    return owner()._package(fullName.substring(0,i));
        else        return owner().rootPackage();
    }

    @Override
    public JClass _extends() {
        return owner().ref(Object.class);
    }

    @Override
    public Iterator<JClass> _implements() {
        return Collections.<JClass>emptyList().iterator();
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings) {
        return this;
    }

    @Override
    public int hashCode() {
        return this.fullName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof JDirectClassEx)) return false;
        return fullName.equals(((JDirectClassEx) obj).fullName);
    }

}
