import 'package:flutter/material.dart';

class StateListScreen extends StatelessWidget {
  const StateListScreen({super.key});

  final List<String> states = const [
    'Uttar Pradesh',
    'Bihar',
    'Maharashtra',
    'West Bengal',
    'Rajasthan',
    'Madhya Pradesh',
    'Karnataka',
    'Gujarat',
    'Tamil Nadu',
    'Andhra Pradesh',
    'Telangana',
    'Kerala',
    'Jharkhand',
    'Punjab',
    'Haryana',
    'Chattisgarh',
    'Odisha',
    'Assam',
    'Uttarakhand',
    'Himachal Pradesh',
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('All States')),
      body: ListView.builder(
        padding: const EdgeInsets.all(8),
        itemCount: states.length,
        itemBuilder: (context, index) {
          return Card(
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: Theme.of(
                  context,
                ).primaryColor.withValues(alpha: 0.1),
                child: Text(
                  states[index][0],
                  style: TextStyle(
                    color: Theme.of(context).primaryColor,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              title: Text(
                states[index],
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              trailing: ElevatedButton(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text(
                        'Downloading ${states[index]} Voter List...',
                      ),
                    ),
                  );
                },
                child: const Text('Download'),
              ),
            ),
          );
        },
      ),
    );
  }
}
