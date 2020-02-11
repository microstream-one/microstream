import '@polymer/polymer/lib/elements/custom-style.js';

const documentContainer = document.createElement('template');

documentContainer.innerHTML = `<custom-style>
      <style>
        :host [theme~="dark"] 
		{ 
			
		}
      </style>
    </custom-style>`;

document.head.appendChild(documentContainer.content);
