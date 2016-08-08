<b>Vorwort.</b> Mein erster Versuch an einem eigenen IRC-artigen Chat Client. Da die Softwareentwicklung in meinem aktuellen Studium etwas weniger behandelt wird, als ich es mir eigentlich wünschen würde, habe ich mir viel selbst beibringen müssen, somit bitte ich um Nachsicht, wenn nicht immer eine klare Struktur erkennbar ist und ebenfalls nicht immer die erwartete Funktionalität geboten ist.

<b>Funktionsweise:</b>
  - Die Serverseite sieht einen Authentifizierungs- und eigentlichen Chat Server vor
  - Der Auth.server authentifiziert Benutzer anhand einer Whitelist
  - Der Client arbeitet einen einfachen  Handshake mit dem Auth.server ab, wird eine Erfolgsnachricht empfangen,
  verbindet sich der Client mit dem Chat Server
  - Der Chat Server unterstützt Multithreading auf der trivialsten Ebene, für jeden verbundenen Client wird ein eigener Thread
  und entsprechend ein Socket geöffnet
  - Gesendete Nachrichten werden vom Chat Server entgegengenommen und an alle verbundenen Clients gebroadcastet
  
<b>Anmerkung.</b> Client und Server "harmonieren" noch nicht richtig miteinander. Dies wird in der nächsten Version behoben.
Der auslösende Fehler ist identifiziert und wird ASAP behoben.


Die zugehörige Serversoftware findet sich unter: https://github.com/Venron/TurtleCMD
