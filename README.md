[aviso de sueño.json](https://github.com/user-attachments/files/23113381/aviso.de.sueno.json)# Proyecto Final | FinanceCoach
Tema: Plan de gastos, ahorros y sueños.

1. Video de la Fase 1
Link: https://drive.google.com/file/d/14c6OkfgD9CVdprC9ddbF7WvWoAatGr92/view?usp=sharing

2. App Android | FinanceCoach
  - Descripción: FinanceCoach es una app móvil diseñada para ser tu compañero personal en el manejo de tus finanzas. Su propósito principal es ayudarte a organizar y controlar tus gastos, planificar tus ahorros y establecer metas financieras concretas, como comprar un carro, viajar o realizar cualquier sueño que tengas.

  - Explicación de Integración con n8n y capturas en el Documetno de Propuesta.

  - Instrucciones de Instalación:

3. Flujo de n8n (archivo .json)
[Uplo{
  "name": "aviso de sueño",
  "nodes": [
    {
      "parameters": {
        "httpMethod": "POST",
        "path": "8c011051-0646-4787-9a41-8b5bcf336f55",
        "options": {}
      },
      "type": "n8n-nodes-base.webhook",
      "typeVersion": 2.1,
      "position": [
        -416,
        64
      ],
      "id": "e6c09b72-8808-498b-bd10-94d3735deb95",
      "name": "Webhook",
      "webhookId": "8c011051-0646-4787-9a41-8b5bcf336f55"
    },
    {
      "parameters": {
        "promptType": "define",
        "text": "=Eres **FinanBot**, un asesor financiero virtual profesional y confiable.  \nTu tarea es enviar mensajes automáticos cuando el progreso de un sueño o meta financiera **supera el 80%**.\n\nRecibes estos datos:\nNombreDelSueno: {{$json.body.nombre}}\nDescripcion: {{$json.body.descripcion}}\nObjetivo: {{$json.body.objetivo}}\nAcumulado: {{$json.body.acumulado}}\nProgreso: {{$json.body.progreso}}\nCorreo: {{$json.body.correo}}\n\nGenera un JSON válido únicamente si el progreso es **mayor al 80%**, con los campos:\n{\n  \"Correo\": {{$json.body.correo}},\n  \"NombreDelSueno\": {{$json.body.nombre}},\n  \"Mensaje\": \"El sueño '{{$json.body.nombre}}' ha alcanzado un progreso del {{$json.body.progreso}}%. Has acumulado Q{{$json.body.acumulado}} de un objetivo de Q{{$json.body.objetivo}}. 🌟 {{$json.body.descripcion}} 💪 ¡Sigue así, estás muy cerca de alcanzar tu meta!\"\n}\n\nSi el progreso es **80% o menos**, **no generes nada**.\n\nInstrucciones:\n- Devuelve **solo JSON válido**.\n- No incluyas explicaciones ni texto adicional.\n- Mensaje en **texto plano**, listo para enviar al usuario.\n",
        "options": {
          "systemMessage": "Eres **FinanBot**, un asesor financiero virtual amable y motivador.  \nTu tarea es informar al usuario cuánto porcentaje lleva ahorrado en su meta y enviarle un mensaje motivacional, de manera clara y profesional.  \n\nRecibes estos datos:\nnombre: {{$json[\"nombre\"]}}\ndescripcion: {{$json[\"descripcion\"]}}\nobjetivo: {{$json[\"objetivo\"]}}\nacumulado: {{$json[\"acumulado\"]}}\nprogreso: {{$json[\"progreso\"]}}\ncorreo: {{$json[\"correo\"]}}\n\nInstrucciones:\n- No incluyas saludos con el nombre del sueño.\n- No incluyas explicaciones adicionales.\n- No uses formato JSON ni HTML.\n- Devuelve únicamente un mensaje motivador con los valores numéricos recibidos."
        }
      },
      "type": "@n8n/n8n-nodes-langchain.agent",
      "typeVersion": 2.2,
      "position": [
        160,
        -80
      ],
      "id": "4647fbc5-5905-43b3-be7d-86df01cbd936",
      "name": "AI Agent"
    },
    {
      "parameters": {
        "model": {
          "__rl": true,
          "mode": "list",
          "value": "gpt-4.1-mini"
        },
        "options": {}
      },
      "type": "@n8n/n8n-nodes-langchain.lmChatOpenAi",
      "typeVersion": 1.2,
      "position": [
        192,
        144
      ],
      "id": "0a597177-1e4a-4474-bcc7-74886d56e07e",
      "name": "OpenAI Chat Model",
      "credentials": {
        "openAiApi": {
          "id": "QCcwkFGjzHDCoiLL",
          "name": "OpenAi account 3"
        }
      }
    },
    {
      "parameters": {
        "sendTo": "={{ $('Webhook').item.json.body.correo }}",
        "subject": "=progreso del sueño registrado ",
        "emailType": "text",
        "message": "={{ $('Webhook').item.json.body[\"nombredelsueño\"] }}{{ $('Webhook').item.json.body.descripcion }}\n{{ $('Webhook').item.json.body.objetivo }}{{ $('Webhook').item.json.body.acumulado }}{{ $('Webhook').item.json.body.acumulado }}{{ $('Webhook').item.json.body.correo }}",
        "options": {}
      },
      "type": "n8n-nodes-base.gmail",
      "typeVersion": 2.1,
      "position": [
        528,
        -80
      ],
      "id": "a12356e6-e024-4db3-9fc6-78e0780ac218",
      "name": "Send a message",
      "webhookId": "bedd68eb-c0f8-4be5-aeca-3559a0996a02",
      "credentials": {
        "gmailOAuth2": {
          "id": "MDDBLI0bvJAEr4tJ",
          "name": "Gmail account 2"
        }
      }
    },
    {
      "parameters": {
        "conditions": {
          "options": {
            "caseSensitive": true,
            "leftValue": "",
            "typeValidation": "strict",
            "version": 2
          },
          "conditions": [
            {
              "id": "21c033db-9874-4465-8c34-e4667f853848",
              "leftValue": "={{ $json.body.progreso }}",
              "rightValue": 80,
              "operator": {
                "type": "number",
                "operation": "gte"
              }
            }
          ],
          "combinator": "and"
        },
        "options": {}
      },
      "type": "n8n-nodes-base.if",
      "typeVersion": 2.2,
      "position": [
        -192,
        64
      ],
      "id": "2fd72221-4cd7-4c61-8f77-970c54ee651f",
      "name": "If"
    }
  ],
  "pinData": {},
  "connections": {
    "Webhook": {
      "main": [
        [
          {
            "node": "If",
            "type": "main",
            "index": 0
          }
        ]
      ]
    },
    "AI Agent": {
      "main": [
        [
          {
            "node": "Send a message",
            "type": "main",
            "index": 0
          }
        ]
      ]
    },
    "OpenAI Chat Model": {
      "ai_languageModel": [
        [
          {
            "node": "AI Agent",
            "type": "ai_languageModel",
            "index": 0
          }
        ]
      ]
    },
    "If": {
      "main": [
        [
          {
            "node": "AI Agent",
            "type": "main",
            "index": 0
          }
        ]
      ]
    }
  },
  "active": false,
  "settings": {
    "executionOrder": "v1"
  },
  "versionId": "5434d958-83af-4c12-9c8d-ca41dd1994a6",
  "meta": {
    "instanceId": "25c1e5fcf5908e163691d5208a8a3d6996455dfeee360d260498dacd46f1c47c"
  },
  "id": "wSXalEbCfDT14edf",
  "tags": []
}ading aviso de sueño.json…]()


4. Documentación de Propuesta
En el repositorio.
