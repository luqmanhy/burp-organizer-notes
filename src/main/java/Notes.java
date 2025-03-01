import java.util.Objects;

public record Notes(String notes) {
    @Override
    public String toString() {
        return notes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Notes notes = (Notes) obj;
        return Objects.equals(this.notes, notes.notes);
    }
}
