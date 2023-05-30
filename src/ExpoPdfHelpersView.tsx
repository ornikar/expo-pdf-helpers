import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

import { ExpoPdfHelpersViewProps } from './ExpoPdfHelpers.types';

const NativeView: React.ComponentType<ExpoPdfHelpersViewProps> =
  requireNativeViewManager('ExpoPdfHelpers');

export default function ExpoPdfHelpersView(props: ExpoPdfHelpersViewProps) {
  return <NativeView {...props} />;
}
