public class LoxInstance {
    
    private LoxClass klass;

    LoxInstance(LoxClass klass){
        this.klass = klass;
    }

    public String toString(){
        return klass.toString() + " instance";
    }

}
