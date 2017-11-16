package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

public class MicrosoftTeamsNotifier extends AbstractEventNotifier{

    /*
    {
      "summary": "Card \"Test card\"",
      "themeColor": "0078D7",
      "title": "Card created: \"Name of card\"",
      "sections": [
        {
          "activityTitle": "David Claux",
          "activitySubtitle": "9/13/2016, 3:34pm",
          "activityImage": "http://stormpath.com/wp-content/uploads/2016/05/spring-boot-logo.jpg",
          "facts": [
            {
              "name": "Board:",
              "value": "Name of board"
            },
            {
              "name": "List:",
              "value": "Name of list"
            },
            {
              "name": "Assigned to:",
              "value": "(none)"
            },
            {
              "name": "Due date:",
              "value": "(none)"
            }
          ],
          "text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        }
      ],
      "potentialAction": [
        {
          "@type": "ActionCard",
          "name": "Set due date",
          "inputs": [
            {
              "@type": "DateInput",
              "id": "dueDate",
              "title": "Select a date"
            }
          ],
          "actions": [
            {
              "@type": "HttpPOST",
              "name": "OK",
              "target": "https://..."
            }
          ]
        },
        {
          "@type": "ActionCard",
          "name": "Move",
          "inputs": [
            {
              "@type": "MultichoiceInput",
              "id": "move",
              "title": "Pick a list",
              "choices": [
                { "display": "List 1", "value": "l1" },
                { "display": "List 2", "value": "l2" }
              ]
            }
          ],
          "actions": [
            {
              "@type": "HttpPOST",
              "name": "OK",
              "target": "https://..."
            }
          ]
        },
        {
          "@type": "ActionCard",
          "name": "Add a comment",
          "inputs": [
            {
              "@type": "TextInput",
              "id": "comment",
              "isMultiline": true,
              "title": "Enter your comment"
            }
          ],
          "actions": [
            {
              "@type": "HttpPOST",
              "name": "OK",
              "target": "https://..."
            }
          ]
        },
        {
          "@type": "OpenUri",
          "name": "View in Trello",
          "targets": [
            { "os": "default", "uri": "https://..." }
          ]
        }
      ]
    }
     */


    @Override
    protected void doNotify(ClientApplicationEvent event) throws Exception {

    }
}
