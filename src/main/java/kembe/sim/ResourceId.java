package kembe.sim;

public class ResourceId {

    public final String id;

    public ResourceId(String id) {
        this.id = id;
    }

    public static ResourceId fromString(String id){
        return new ResourceId( id );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceId that = (ResourceId) o;

        if (!id.equals( that.id )) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override public String toString() {
        return "ResourceId( " + id +" )";
    }
}
