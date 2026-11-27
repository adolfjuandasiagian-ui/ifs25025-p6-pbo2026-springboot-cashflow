package org.delcom.ifs25025springbootcashflow.framework.view;

import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.List;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.framework.view.auth.LoginView;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.domain.entity.AiGeneratedTransaction;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "cashflow", layout = MainLayout.class)
@PageTitle("Cashflow | Transaksi")
public class FinanceView extends VerticalLayout implements BeforeEnterObserver {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final FinanceUseCase financeUseCase;
    private final AuthSession authSession;
    private final FinancePresenter presenter;
    private final FinanceAiUseCase financeAiUseCase;
    private final FileStorage fileStorage;

    private final Span incomeValue = new Span();
    private final Span expenseValue = new Span();
    private final Span balanceValue = new Span();
    private final Grid<Transaction> transactionGrid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField("Cari keterangan");
    private final ComboBox<TransactionTypeFilter> typeFilter =
            new ComboBox<>("Jenis transaksi");
    private final ComboBox<SortOption> sortOption = new ComboBox<>("Urutkan");

    public FinanceView(
            FinanceUseCase financeUseCase,
            AuthSession authSession,
            FinancePresenter presenter,
            FinanceAiUseCase financeAiUseCase,
            FileStorage fileStorage) {
        this.financeUseCase = financeUseCase;
        this.authSession = authSession;
        this.presenter = presenter;
        this.financeAiUseCase = financeAiUseCase;
        this.fileStorage = fileStorage;

        setSizeFull();

        typeFilter.setItems(TransactionTypeFilter.values());
        typeFilter.setItemLabelGenerator(filter -> switch (filter) {
            case ALL -> "Semua";
            case INCOME -> "Pemasukan";
            case EXPENSE -> "Pengeluaran";
        });
        typeFilter.setValue(TransactionTypeFilter.ALL);

        sortOption.setItems(SortOption.values());
        sortOption.setItemLabelGenerator(SortOption::label);
        sortOption.setValue(SortOption.NEWEST_FIRST);

        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(event -> refreshData());
        typeFilter.addValueChangeListener(event -> refreshData());
        sortOption.addValueChangeListener(event -> refreshData());

        transactionGrid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setAutoWidth(true)
                .setFlexGrow(1);
        transactionGrid.addColumn(transaction ->
                        transaction.getType() == TransactionType.INCOME
                                ? "Pemasukan"
                                : "Pengeluaran")
                .setHeader("Jenis")
                .setAutoWidth(true);
        transactionGrid.addColumn(transaction ->
                        presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah")
                .setAutoWidth(true);
        transactionGrid.addColumn(transaction ->
                        transaction.getCreatedAt().format(DATE_FORMAT))
                .setHeader("Tanggal")
                .setAutoWidth(true);
        transactionGrid.addComponentColumn(this::createProofPreview)
                .setHeader("Bukti")
                .setAutoWidth(true);
        transactionGrid.addComponentColumn(this::createDeleteButton)
                .setHeader("Aksi")
                .setAutoWidth(true);

        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total pemasukan", incomeValue),
                createSummaryCard("Total pengeluaran", expenseValue),
                createSummaryCard("Saldo", balanceValue));
        summary.setWidthFull();

        HorizontalLayout filters = new HorizontalLayout(
                searchField, typeFilter, sortOption);
        filters.setWidthFull();

        Button addTransactionButton = new Button(
                "Tambah transaksi",
                event -> openCreateDialog());
        Button generateAiButton = new Button(
                "Generate AI",
                event -> openAiDialog());

        add(
                new H1("Ringkasan Keuangan"),
                summary,
                new HorizontalLayout(addTransactionButton, generateAiButton),
                filters,
                transactionGrid);
        setFlexGrow(1, transactionGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authSession.currentUser().isEmpty()) {
            event.rerouteTo(LoginView.class);
            return;
        }

        refreshData();
    }

    private void refreshData() {
        int userId = authSession.currentUser()
                .orElseThrow(() -> new IllegalStateException("Sesi pengguna tidak ditemukan"))
                .getId();

        incomeValue.setText(presenter.formatMoney(
                financeUseCase.getTotalIncome(userId)));
        expenseValue.setText(presenter.formatMoney(
                financeUseCase.getTotalExpense(userId)));
        balanceValue.setText(presenter.formatMoney(
                financeUseCase.getBalance(userId)));

        transactionGrid.setItems(financeUseCase.listTransactions(
                userId,
                searchField.getValue(),
                typeFilter.getValue(),
                sortOption.getValue()));
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Tambah transaksi");
        dialog.setWidth("400px");

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setRequired(true);
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (rupiah)");
        amountField.setRequired(true);
        amountField.setHelperText("Masukkan angka bulat, contoh: 150000");
        amountField.setWidthFull();

        ComboBox<TransactionType> typeField = new ComboBox<>("Jenis transaksi");
        typeField.setItems(TransactionType.values());
        typeField.setItemLabelGenerator(type -> switch (type) {
            case INCOME -> "Pemasukan";
            case EXPENSE -> "Pengeluaran";
        });
        typeField.setRequired(true);
        typeField.setWidthFull();

        MemoryBuffer proofBuffer = new MemoryBuffer();
        Upload proofUpload = new Upload(proofBuffer);
        proofUpload.setAcceptedFileTypes(
                "image/jpeg", "image/png", "image/gif", "image/webp");
        proofUpload.setMaxFiles(1);
        proofUpload.setWidthFull();

        Button cancelButton = new Button("Batal", event -> dialog.close());
        Button saveButton = new Button("Simpan", event -> {
            try {
                long amount = Long.parseLong(amountField.getValue().trim());

                String proofPath = null;
                if (proofBuffer.getFileName() != null
                        && !proofBuffer.getFileName().isBlank()) {
                    proofPath = fileStorage.store(
                            proofBuffer.getFileName(),
                            proofBuffer.getInputStream());
                }

                financeUseCase.createTransaction(
                        currentUserId(),
                        descriptionField.getValue(),
                        amount,
                        typeField.getValue(),
                        proofPath);

                dialog.close();
                refreshData();
                Notification.show("Transaksi berhasil ditambahkan");
            } catch (NumberFormatException exception) {
                Notification.show("Jumlah harus berupa angka bulat");
            } catch (IOException exception) {
                Notification.show("Gagal menyimpan bukti: " + exception.getMessage());
            } catch (DomainException exception) {
                Notification.show(exception.getMessage());
            }
        });

        VerticalLayout form = new VerticalLayout(
                descriptionField,
                amountField,
                typeField,
                proofUpload);
        form.setPadding(false);
        dialog.add(form);
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private int currentUserId() {
        return authSession.currentUser()
                .orElseThrow(() -> new IllegalStateException("Sesi pengguna tidak ditemukan"))
                .getId();
    }

    private Component createProofPreview(Transaction transaction) {
        if (transaction.getProofPath() == null) {
            return new Span("-");
        }

        Image image = new Image(
                "/uploads/" + transaction.getProofPath(),
                "Bukti transaksi");
        image.setWidth("48px");
        image.setHeight("48px");
        image.getStyle().set("object-fit", "cover");
        image.getElement().getStyle().set("cursor", "pointer");
        image.addClickListener(event ->
                new ImagePreviewDialog(
                        "Bukti: " + transaction.getDescription(),
                        "/uploads/" + transaction.getProofPath()).open());
        return image;
    }

    private Component createDeleteButton(Transaction transaction) {
        Button delete = new Button("Hapus", event -> openDeleteDialog(transaction));
        delete.getStyle().set("cursor", "pointer");
        return delete;
    }

    private void openDeleteDialog(Transaction transaction) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Konfirmasi hapus");
        dialog.add(new Span("Hapus transaksi \"" + transaction.getDescription() + "\"?"));

        Button cancel = new Button("Batal", event -> dialog.close());
        Button confirm = new Button("Hapus", event -> {
            try {
                financeUseCase.deleteTransaction(transaction.getId(), currentUserId());
                dialog.close();
                refreshData();
                Notification.show("Transaksi berhasil dihapus");
            } catch (DomainException exception) {
                Notification.show(exception.getMessage());
            }
        });
        dialog.getFooter().add(cancel, confirm);
        dialog.open();
    }

    private void openAiDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Generate transaksi dengan AI");
        dialog.setWidth("700px");

        TextField prompt = new TextField("Prompt");
        prompt.setWidthFull();
        prompt.setPlaceholder("Contoh: gaji 5000000 dan beli makan 75000");

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Grid<AiGeneratedTransaction> preview =
                new Grid<>(AiGeneratedTransaction.class, false);
        preview.addColumn(AiGeneratedTransaction::description)
                .setHeader("Keterangan")
                .setFlexGrow(1);
        preview.addColumn(item -> presenter.formatMoney(item.amount()))
                .setHeader("Jumlah");
        preview.addColumn(item -> item.type() == TransactionType.INCOME
                ? "Pemasukan" : "Pengeluaran")
                .setHeader("Jenis");

        Button process = new Button("Proses AI");
        process.addClickListener(event -> {
            progress.setVisible(true);
            process.setEnabled(false);
            try {
                preview.setItems(financeAiUseCase.generateTransactions(prompt.getValue()));
            } catch (DomainException exception) {
                Notification.show(exception.getMessage());
            } finally {
                progress.setVisible(false);
                process.setEnabled(true);
            }
        });

        Button save = new Button("Simpan semua", event -> {
            List<AiGeneratedTransaction> items =
                    preview.getGenericDataView().getItems().toList();
            if (items.isEmpty()) {
                Notification.show("Tidak ada hasil untuk disimpan");
                return;
            }
            try {
                for (AiGeneratedTransaction item : items) {
                    financeUseCase.createTransaction(
                            currentUserId(),
                            item.description(),
                            item.amount(),
                            item.type(),
                            null);
                }
                dialog.close();
                refreshData();
                Notification.show("Transaksi AI berhasil disimpan");
            } catch (DomainException exception) {
                Notification.show(exception.getMessage());
            }
        });

        Button cancel = new Button("Batal", event -> dialog.close());
        dialog.add(prompt, progress, process, preview);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private VerticalLayout createSummaryCard(String label, Span value) {
        VerticalLayout card = new VerticalLayout(new Span(label), value);
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");
        return card;
    }
}