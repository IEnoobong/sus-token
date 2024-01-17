package co.enoobong.services.views.sendproof;

import co.enoobong.services.data.entities.SusActionEntity;
import co.enoobong.services.services.SusActionService;
import co.enoobong.services.views.MainLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
@PageTitle("Send EcoResponsible Proof")
@Route(value = "send-proof", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class SendProofView extends HorizontalLayout {

  @SneakyThrows
  public SendProofView(SusActionService susActionService) {
    var walletAddress = new TextField("Wallet Address");
    walletAddress.setPattern("^0x[a-fA-F0-9]{40}$");
    walletAddress.setRequired(true);

    var description = new TextField("Description");
    description.setMinLength(20);
    description.setRequired(true);

    var uploadLabel = new NativeLabel("Upload your proof");

    var fileBuffer = new FileBuffer();
    var actionProof = new Upload(fileBuffer);
    actionProof.setAutoUpload(false);
    actionProof.setDropAllowed(false);
    actionProof.setMaxFiles(1);
    actionProof.setMaxFileSize(1024 * 1000);
    actionProof.setAcceptedFileTypes("image/png");
    actionProof.addFileRejectedListener(fileRejectedEvent -> {
      log.error("File was rejected {}", fileRejectedEvent.getErrorMessage());
      var notification = new Notification(fileRejectedEvent.getErrorMessage(), 5000,
          Position.TOP_END);
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

      notification.open();
    });
    actionProof.addFailedListener(failedEvent -> {
      log.error("Failed to upload file {}", failedEvent);
    });

    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setClassName("button-layout");
    var cancel = new Button("Cancel");
    var save = new Button("Submit");
    cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    save.addClickShortcut(Key.ENTER);
    save.addClickListener(buttonClickEvent -> {
      actionProof.getElement().callJsFunction("uploadFiles");
      actionProof.addSucceededListener(succeededEvent -> {
        log.info("File upload succeeded {}", succeededEvent);
        var destination = Paths.get(Paths.get("").toAbsolutePath().toString(),
            "/proofs/" + RandomStringUtils.randomAlphabetic(9) + ".png");
        try {
          Files.createDirectories(destination.getParent());
          Files.copy(fileBuffer.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        var susAction = SusActionEntity.builder()
            .walletAddress(walletAddress.getValue())
            .description(description.getValue())
            .proofData(destination.toString())
            .build();
        susActionService.createSusAction(susAction);

        var notification = new Notification(
            "Your proof has been received. An admin will evaluate it shortly.", 5000,
            Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        notification.open();
      });
    });
    buttonLayout.add(save, cancel);

    FormLayout formLayout = new FormLayout();
    formLayout.add(walletAddress, description);
    formLayout.addFormItem(actionProof, uploadLabel);
    formLayout.setResponsiveSteps(
        // Use one column by default
        new ResponsiveStep("0", 1),
        // Use two columns, if layout's width exceeds 500px
        new ResponsiveStep("500px", 2));
    formLayout.add(buttonLayout);

    setMargin(true);

    add(formLayout);
  }

}
