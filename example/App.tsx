import { Image, StyleSheet, Text, View } from "react-native";
import { downloadAsync, documentDirectory } from "expo-file-system";
import * as ExpoPdfHelpers from "expo-pdf-helpers";
import { useEffect, useState } from "react";

export default function App() {
  const [preview, setPreview] = useState<string>();
  const [pageCount, setPageCount] = useState<number>();

  useEffect(() => {
    downloadAsync(
      "https://www.africau.edu/images/default/sample.pdf",
      documentDirectory + "downloaded.pdf"
    ).then(({uri}) => {
      ExpoPdfHelpers.generateThumbnail(uri, 1).then(({uri: previewUri}) => {
        setPreview(previewUri);
      });
      ExpoPdfHelpers.getPageCount(uri).then(setPageCount);
    });
  }, []);

  return (
    <View style={styles.container}>
      <Text>Nombre de page: {pageCount ?? "Chargement..."}</Text>
      {preview ? (
        <Image
          source={{ uri: preview }}
          style={{ width: 210, height: 297 }}
          resizeMode="contain"
        />
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },
});
