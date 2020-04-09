
import '@vaadin/vaadin-lumo-styles/all-imports';
import '@vaadin/vaadin-lumo-styles/presets/compact.js';

const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `
<custom-style>
  <style>
    html {
      --lumo-font-size: 1rem;
      --lumo-font-size-xxxl: 1.75rem;
      --lumo-font-size-xxl: 1.375rem;
      --lumo-font-size-xl: 1.125rem;
      --lumo-font-size-l: 1rem;
      --lumo-font-size-m: 0.875rem;
      --lumo-font-size-s: 0.8125rem;
      --lumo-font-size-xs: 0.75rem;
      --lumo-font-size-xxs: 0.6875rem;
      --lumo-line-height-m: 1.4;
      --lumo-line-height-s: 1.2;
      --lumo-line-height-xs: 1.1;
      --lumo-font-family: Consolas, "Andale Mono WT", "Andale Mono", "Lucida Console", "Lucida Sans Typewriter", "DejaVu Sans Mono", "Bitstream Vera Sans Mono", "Liberation Mono", "Nimbus Mono L", Monaco, "Courier New", Courier, monospace;
      --lumo-size-xl: 3rem;
      --lumo-size-l: 2.5rem;
      --lumo-size-m: 2rem;
      --lumo-size-s: 1.75rem;
      --lumo-size-xs: 1.5rem;
      --lumo-space-xl: 1.875rem;
      --lumo-space-l: 1.25rem;
      --lumo-space-m: 0.625rem;
      --lumo-space-s: 0.3125rem;
      --lumo-space-xs: 0.1875rem;
    }
    
    .banner {
    	background-color: #091946;
    	padding: var(--lumo-space-s);
    }
    
    .header {
    	background-image: url('images/header.jpg');
    	background-size: contain;
    	background-position: center bottom;
    	background-repeat: no-repeat;
    }
    
    .box {
    	margin: var(--lumo-space-l);
    	padding: var(--lumo-space-l);
    	background: var(--lumo-base-color) linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    	border-radius: var(--lumo-border-radius);
        box-shadow: 0 0 0 1px var(--lumo-contrast-10pct), var(--lumo-box-shadow-l);
    }
    
    .error {
    	color: #faa
    }

    [theme~="dark"] {
      --lumo-primary-color: #00A0E3;
      --lumo-primary-text-color: #00A0E3;
      --lumo-base-color: #191B28;
    }
  </style>
</custom-style>
`;

document.head.appendChild($_documentContainer.content);
