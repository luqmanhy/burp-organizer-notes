import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.UserInterface;

public class OrganizerExtension implements BurpExtension {
    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName("Organizer Notes");

        NotesStorage notesStorage = new NotesStorage(api.persistence());
        UserInterface ui = api.userInterface();

        NotesManagerTab notesManagerTab = new NotesManagerTab(notesStorage);
        ui.registerSuiteTab("OrgNotes", notesManagerTab.getPanel());

        api.userInterface().registerContextMenuItemsProvider(new SendToOrganizerMenu(api, notesStorage));
    }
}
