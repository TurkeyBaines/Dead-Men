package io.dm.interfaceeditor;

import io.dm.interfaceeditor.cache.CacheSession;
import io.dm.interfaceeditor.ui.MainView;
import java.io.File;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class InterfaceEditorApp extends Application
{
	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage stage)
	{
		stage.setTitle("Dead-Men Interface Editor");

		File cacheDir = promptForCacheDirectory(stage);
		if (cacheDir == null)
		{
			Platform.exit();
			return;
		}

		CacheSession session;
		try
		{
			session = CacheSession.open(cacheDir);
		}
		catch (Exception ex)
		{
			showError("Failed to load cache", "Could not load the cache at:\n" + cacheDir
				+ "\n\n" + ex);
			Platform.exit();
			return;
		}

		MainView mainView = new MainView(stage, session);
		Scene scene = new Scene(mainView, 1280, 820);
		stage.setScene(scene);
		stage.show();

		stage.setOnCloseRequest(e ->
		{
			try
			{
				session.close();
			}
			catch (Exception ignored)
			{
			}
		});
	}

	private File promptForCacheDirectory(Stage owner)
	{
		Alert intro = new Alert(Alert.AlertType.INFORMATION);
		intro.setTitle("Dead-Men Interface Editor");
		intro.setHeaderText("Point to your OSRS cache");
		intro.setContentText("Select the folder containing your Dead-Men cache "
			+ "(main_file_cache.dat2 and the .idx files).\n\n"
			+ "This cache is opened read-only - all edits are saved to a separate output "
			+ "folder you choose later, your live cache is never modified directly.");
		intro.showAndWait();

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Dead-Men cache directory");

		while (true)
		{
			File dir = chooser.showDialog(owner);
			if (dir == null)
			{
				return null;
			}

			File dat2 = new File(dir, "main_file_cache.dat2");
			File idx255 = new File(dir, "main_file_cache.idx255");
			if (!dat2.exists() || !idx255.exists())
			{
				Alert retry = new Alert(Alert.AlertType.WARNING,
					"That folder doesn't look like a valid cache (missing main_file_cache.dat2 "
						+ "or main_file_cache.idx255). Pick a different folder?",
					ButtonType.YES, ButtonType.NO);
				retry.showAndWait();
				if (retry.getResult() == ButtonType.NO)
				{
					return null;
				}
				continue;
			}

			return dir;
		}
	}

	private void showError(String title, String message)
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(title);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
