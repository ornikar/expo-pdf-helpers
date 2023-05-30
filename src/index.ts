// Import the native module. On web, it will be resolved to ExpoPdfHelpersModule.web.ts
// and on native platforms to ExpoPdfHelpersModule.ts
import ExpoPdfHelpers from "./ExpoPdfHelpers";

export async function getPageCount(filePath: string): Promise<number> {
  return await ExpoPdfHelpers.getPageCount(filePath);
}

export type ThumbnailResult = {
  uri: string;
  width: number;
  height: number;
};

export async function generateThumbnail(
  filePath: string,
  page: number,
  quality?: number
): Promise<ThumbnailResult> {
  return await ExpoPdfHelpers.generateThumbnail(
    filePath,
    page,
    quality || 100
  );
}