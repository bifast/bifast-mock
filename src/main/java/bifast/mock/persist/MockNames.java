package bifast.mock.persist;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MockNames {
    @Id 
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArbtrFullName () {
        return "Frans Adam";
    }


}
