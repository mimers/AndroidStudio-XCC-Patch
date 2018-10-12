package li.joker;

import com.android.ide.common.repository.ResourceVisibilityLookup;

public class AndroidStudioXMLCodeCompletionPatch {
    private static ResourceVisibilityLookup ALL = new ResourceVisibilityLookup() {
        @Override
        public boolean isPrivate(com.android.resources.ResourceType resourceType, String s) {
            return false;
        }

        @Override
        public boolean isPublic(com.android.resources.ResourceType resourceType, String s) {
            return true;
        }

        @Override
        protected boolean isKnown(com.android.resources.ResourceType resourceType, String s) {
            return true;
        }

        @Override
        public com.android.builder.model.AndroidLibrary getPrivateIn(com.android.resources.ResourceType resourceType, String s) {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    };

    public static ResourceVisibilityLookup fakeTransparentVisibility(ResourceVisibilityLookup visibilityLookup) {
        return ALL;
    }
}
