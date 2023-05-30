import * as React from 'react';

import { ExpoPdfHelpersViewProps } from './ExpoPdfHelpers.types';

export default function ExpoPdfHelpersView(props: ExpoPdfHelpersViewProps) {
  return (
    <div>
      <span>{props.name}</span>
    </div>
  );
}
