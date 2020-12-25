const functions = require('firebase-functions');
const admin = require('firebase-admin');
const firebase_tools = require('firebase-tools');

admin.initializeApp();

/*exports.deleteExpiredChat = functions.pubsub.schedule('* * * * *').onRun(context => {
    return admin.firestore().collection('chats').get().then(response => {
        return response.forEach(snapshot => {
            var expireTime = snapshot.data().finishTime;
            var peopleInChat = snapshot.data().peopleInChat;
            var chatId = snapshot.id;
            var expireInMillies = expireTime._seconds * 1000;
            var now = new Date();
            var nowInMillies = now.getTime();

            if (nowInMillies >= expireInMillies) {    
                var path = "chats/" + chatId + "/messages"            
                return firebase_tools.firestore.delete(path, {
                    project: process.env.GCLOUD_PROJECT,
                    recursive: true,
                    yes: true
                }).then(reponse => {
                    return admin.firestore().doc("chats/" + chatId).delete()
                })
            } else return null
        })
    })
})*/

/*exports.deleteEmptyChat = functions.pubsub.schedule('* * * * *').onRun(context => {
    return admin.firestore().collection('chats').get().then(response => {
        return response.forEach(snapshot => {
            var chatId = snapshot.id;
            var peopleInChat = snapshot.data().peopleInChat;
            if (peopleInChat.length === 0) {
                return admin.firestore().doc('chats/' + chatId).delete()
            } else return null
        })
    })
})*/

exports.clearActiveChats = functions.firestore.document('chats/{chatId}').onDelete((change,context) => {
    var chatId = context.params.chatId;
    var peopleInChat = change.data().peopleInChat

    if (peopleInChat.length > 0) {
        for (i = 0 ; i < peopleInChat.length ; i++) {
            var userId = peopleInChat[i]
            var newActiveChat = ""
            admin.firestore().doc('users/' + userId).update({activeChat:newActiveChat})
        }
    }
    return null
})

exports.handleMonthlySubscription = functions.pubsub.topic('play_notifications').onPublish((message) => {
    var encodedString = new Buffer(message.data,'base64').toString('ascii')
    var encodedObject = JSON.parse(encodedString)
    if (encodedObject.subscriptionNotification !== null) {
        if (encodedObject.subscriptionNotification.notificationType === 3 || encodedObject.subscriptionNotification.notificationType === 12) {
            return admin.firestore().doc('purchaseTokens/' + encodedObject.subscriptionNotification.purchaseToken).get().then(response => {
                var userId = response.data().userId;
                return admin.firestore().doc('users/' + userId).get().then(response2 => {
                    var premiumBegin = response2.data().premiumBegin;
                    if (premiumBegin !== null) {
                        var date = new Date(premiumBegin)
                        var premiumBeginInMillies = date.getTime()
                        var oneDayInMillies = 1000*60*60*24;
                        var oneMonthInMillies = oneDayInMillies*30;                        
                        var expireInMillies = oneMonthInMillies + premiumBeginInMillies;
                        return admin.firestore().doc('users/' + userId).update({premiumBegin:admin.firestore.FieldValue.delete(),premiumExpire:expireInMillies}).then(response => {
                            return admin.firestore().doc('purchaseTokens/' + encodedObject.subscriptionNotification.purchaseToken).delete()
                        })
                    } else return null
                })
            })
        } else if (encodedObject.subscriptionNotification.notificationType === 2 || encodedObject.subscriptionNotification.notificationType === 4) {
            console.log(encodedObject.subscriptionNotification.notificationType + " , " + encodedObject.subscriptionNotification.purchaseToken)
            return admin.firestore().doc('purchaseTokens/' + encodedObject.subscriptionNotification.purchaseToken).get().then(response => {
                var userId = response.data().userId;
                return admin.firestore().doc('users/' + userId).update({premiumBegin:encodedObject.eventTimeMillis});
            })
        } else return null
    } else return null
})

/*exports.premiumExpire = functions.pubsub.schedule('* * * * *').onRun(context => {
    return admin.firestore().collection('users').get().then(response => {
        return response.forEach(snapshot => {
            var premiumExpire = snapshot.data().premiumExpire;
            var userId = snapshot.id;

            if (premiumExpire !== null) {                
                var expireInMillies = premiumExpire._seconds * 1000;
                var now = new Date();
                var nowInMillies = now.getTime();
                if (nowInMillies >= expireInMillies) {
                    return admin.firestore().doc('users/' + userId).update({isPremium:false,premiumExpire:admin.firestore.FieldValue.delete()})
                } else return null
            } else return null
        })
    })
})*/

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
