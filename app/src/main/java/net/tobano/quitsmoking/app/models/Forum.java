package net.tobano.quitsmoking.app.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Forum {
    public Long idCategory;
    public Long idLanguage;
    public String name;

    public Forum() {
        // Default constructor required for calls to DataSnapshot.getValue(Forum.class)
    }

    public Forum(Long idCategory, Long idLanguage, String name) {
        this.idCategory = idCategory;
        this.idLanguage = idLanguage;
        this.name = name;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("idCategory", idCategory);
        result.put("idLanguage", idLanguage);
        result.put("name", name);

        return result;
    }
    // [END post_to_map]

    @Override
    public String toString() {
        // to populate spinner on NewPostActivity
        return name;
    }

}
// [END post_class]
